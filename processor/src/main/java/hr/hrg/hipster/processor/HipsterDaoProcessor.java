package hr.hrg.hipster.processor;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static hr.hrg.javapoet.PoetUtil.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.persistence.*;
import javax.tools.*;
import javax.tools.Diagnostic.Kind;

import com.squareup.javapoet.*;
import com.squareup.javapoet.TypeSpec.*;

import hr.hrg.hipster.dao.*;
import hr.hrg.hipster.sql.*;

@SupportedAnnotationTypes("hr.hrg.hipster.dao.HipsterEntity")
@SupportedOptions({"hipster_proc_jackson","hipster_proc_builder", "hipster_proc_column_meta_class"})
public class HipsterDaoProcessor extends AbstractProcessor{

    @Override
    public SourceVersion getSupportedSourceVersion() {
        // We may claim to support the latest version, since we are not using
        // any version-specific extensions.
        return SourceVersion.latest();
    }
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(HipsterEntity.class);
		List<EntityDef> defs = new ArrayList<EntityDef>();
		Map<String,List<EntityDef>> defMap = new HashMap<>(); 
		processingEnv.getMessager().printMessage(Kind.NOTE, "process classes "+elements);
		for (Element element : elements) {
			if		(element.getKind() == ElementKind.INTERFACE) {
				EntityDef def = generateClass((TypeElement) element, processingEnv);
				defs.add(def);
				List<EntityDef> list = defMap.get(def.packageName);
				if(list == null) {
					list = new ArrayList<>();
					defMap.put(def.packageName, list);
				}
				list.add(def);
			}else{
				processingEnv.getMessager().printMessage(Kind.NOTE, "skip because not interface "+element);				
			}
		}
		
		for(Entry<String, List<EntityDef>> entry:defMap.entrySet()) {
			generateAllEntitiesInPackage(entry.getKey(), entry.getValue(), processingEnv);
		}
		
		return false;
	}

	private void generateAllEntitiesInPackage(String packageName, List<EntityDef> defs, ProcessingEnvironment processingEnv) {
		ClassName className  = ClassName.get(packageName,"AllEntitiesInPackage");
		
		TypeSpec.Builder cp = classBuilder(className);

		com.squareup.javapoet.CodeBlock.Builder codeBlock = CodeBlock.builder();
		codeBlock.add("$T.toArray(\n", HipsterSqlUtil.class);
		codeBlock.indent();
		for(int i=0; i<defs.size(); i++) {
			codeBlock.add("$T.class", defs.get(i).typeMeta);
			if(i != defs.size()-1) codeBlock.add(",\n");
		}
		codeBlock.unindent();
		final CodeBlock code1 = codeBlock.add("\n)").build();
		
		// Class<? extends IEntityMeta> ALL_META = HipsterSqlUtil.toArray(...);
		addField(cp, 
			ArrayTypeName.of(parametrized(Class.class, WildcardTypeName.subtypeOf(IEntityMeta.class))), 
			"ALL_META", 
			field -> {
				field.addModifiers(PUBLIC().STATIC().FINAL().toArray());
				field.initializer(code1);
		} );	
		
		codeBlock = CodeBlock.builder();
		codeBlock.add("$T.toArray(\n", HipsterSqlUtil.class);
		codeBlock.indent();
		for(int i=0; i<defs.size(); i++) {
			codeBlock.add("$T.class", defs.get(i).type);
			if(i != defs.size()-1) codeBlock.add(",\n");
		}
		codeBlock.unindent();
		final CodeBlock code2 = codeBlock.add("\n)").build();
		
		// Class<? extends IEntityMeta> ALL_ENTITIES = HipsterSqlUtil.toArray(...);
		addField(cp, 
			ArrayTypeName.of(Class.class), 
			"ALL_ENTITIES", 
			field -> {
				field.addModifiers(PUBLIC().STATIC().FINAL().toArray());
				field.initializer(code2);
		} );
		
		write(className,cp.build(),processingEnv);
	}

	private EntityDef generateClass(TypeElement clazz, ProcessingEnvironment processingEnv) {
		
   		boolean jackson = "true".equalsIgnoreCase(processingEnv.getOptions().get("hipster_proc_jackson"));
        boolean genBuilder = "true".equalsIgnoreCase(processingEnv.getOptions().get("hipster_proc_builder"));
        

        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "annotated class: " + clazz.getQualifiedName());
        
        
        EntityDef def = new EntityDef(clazz, processingEnv.getElementUtils());
        
        for (Element element : clazz.getEnclosedElements()) {
        	if(element.getKind() == ElementKind.METHOD) {
        		ExecutableElement method = (ExecutableElement) element;
        		String name = element.getSimpleName().toString();
        		TypeName typeName = TypeName.get(method.getReturnType());

        		String typeNameStr = typeName.toString();

        		// check if needs to be skipped
        		boolean skip = !name.startsWith("get") && (!name.startsWith("is") && (typeNameStr == "boolean" || typeNameStr == "java.lang.Boolean"));
        		if(skip) continue;
        		HipsterColumn hipsterColumn = element.getAnnotation(HipsterColumn.class);
        		if(hipsterColumn != null && hipsterColumn.skip()) continue;

        		Property prop = def.addProp(name, typeName, method.getReturnType(), method, processingEnv);
        		
        		prop.readOnly = method.getAnnotation(Id.class) != null;

        		if(prop.readOnly) {
        			if(def.getPrimaryProp() != null) {
        				processingEnv.getMessager().printMessage(Kind.ERROR, "Second id field found, frist one was at "+def.getPrimaryProp().getterName+"()", method);        				
        			}else {
        				def.setPrimaryProp(prop);        				
        			}
        		}
        	}
		}
        
        try {
//        	Builder builder = new GenEnum().gen2(def);
//        	write(def.typeEnum.packageName(), builder.build(), processingEnv);
        	
        	String[] className = HipsterProcessorUtil.splitClassName(processingEnv.getOptions().getOrDefault("hipster_proc_column_meta_class","hr.hrg.hipster.sql.ColumnMeta"));
        	ClassName columnMetaBase  = ClassName.get(className[0],className[1]);
        	
        	Builder builder = new GenImmutable(jackson, columnMetaBase).gen2(def);
        	write(def.typeImmutable, builder.build(), processingEnv);

        	if(genBuilder){
        		builder = new GenBuilder(jackson,columnMetaBase).gen2(def);
        		write(def.typeBuilder, builder.build(), processingEnv);
        	}

        	if(def.genUpdate){
				builder = new GenUpdate(jackson, genBuilder,columnMetaBase).gen2(def);
				write(def.typeUpdate, builder.build(), processingEnv);
        	}
    		if(def.genMeta){
    			
    			builder = new GenMeta().gen(def,columnMetaBase);
    			JavaFile javaFile = JavaFile.builder(def.typeDelta.packageName(), builder.build())
    					.addStaticImport(HipsterSqlUtil.class,"annotation")
    					.build();
    			write(def.typeDelta.packageName(), javaFile, processingEnv);
    			
//    			builder = new GenDelta().gen2(def);
//    			write(def.typeDelta, builder.build(), processingEnv);
    		}
        	

        	        	
		} catch (Throwable e) {
			processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage()+"\n"+getTrace(e), clazz);
		}  
        return def;
	}
	
	public void write(ClassName type, TypeSpec spec, ProcessingEnvironment processingEnv) {
		write(type.packageName(), spec, processingEnv);
	}
	
	public void write(String packageName, TypeSpec spec, ProcessingEnvironment processingEnv) {
		write(packageName, JavaFile.builder(packageName, spec).build(), processingEnv);
	}
	
	public void write(String packageName, JavaFile javaFile, ProcessingEnvironment processingEnv) {
		try {
			JavaFileObject jfo = processingEnv.getFiler().createSourceFile(packageName+"."+javaFile.typeSpec.name);
			
			try (	OutputStream out = jfo.openOutputStream();
					PrintWriter pw = new PrintWriter(out);
					){
				javaFile.writeTo(pw);
				pw.flush();
			}		
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}	

	String getTrace(Throwable e){
		try(StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);) {			
			e.printStackTrace(pw);
			return sw.toString();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}
}
