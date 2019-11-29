package hr.hrg.hipster.processor;

import static hr.hrg.javapoet.PoetUtil.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.util.*;
import javax.persistence.*;
import javax.tools.*;
import javax.tools.Diagnostic.Kind;

import com.squareup.javapoet.*;
import com.squareup.javapoet.TypeSpec.*;

import hr.hrg.hipster.entity.*;
import hr.hrg.hipster.sql.*;

@SupportedAnnotationTypes("hr.hrg.hipster.entity.HipsterEntity")
@SupportedOptions({"hipster_proc_jackson","hipster_proc_builder", "hipster_proc_column_meta_class"})
public class HipsterDaoProcessor extends AbstractProcessor{

//	private static Map<String, List<ClassName>> packageMetas = new HashMap<String, List<ClassName>>();
	private static Map<ClassName, EntityDef> defMap = new HashMap<>();

	static int counter= 0;
	static GenOptions genOptions;
	
    @Override
    public SourceVersion getSupportedSourceVersion() {
        // We may claim to support the latest version, since we are not using
        // any version-specific extensions.
        return SourceVersion.latest();
    }
	
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
    	super.init(processingEnv);
		processingEnv.getMessager().printMessage(Kind.NOTE, "INIT "+(++counter));
		boolean jackson = "true".equalsIgnoreCase(processingEnv.getOptions().get("hipster_proc_jackson"));
		boolean genBuilder = "true".equalsIgnoreCase(processingEnv.getOptions().get("hipster_proc_builder"));
		boolean genVisitor = "true".equalsIgnoreCase(processingEnv.getOptions().get("hipster_proc_visitor"));
		boolean genUpdate = "true".equalsIgnoreCase(processingEnv.getOptions().get("hipster_proc_update"));
		
		genOptions = new GenOptions(jackson,true,genVisitor,genUpdate, genBuilder);
    }
    
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(HipsterEntity.class);
		Map<String, Element> packageClasses = new HashMap<>();

        
		
		processingEnv.getMessager().printMessage(Kind.NOTE, "process classes "+elements);
		for (Element element : elements) {
			if (element.getKind() == ElementKind.INTERFACE) {
				EntityDef def = makeDef(element);
				if(def.genOptions.isGenMeta() && def.packageElement != null) packageClasses.put(def.packageName, def.packageElement);

				generateClass(def, processingEnv);

			}else{
				processingEnv.getMessager().printMessage(Kind.NOTE, "skip because not interface "+element);				
			}
		}
		
		for(Entry<String, Element> entry: packageClasses.entrySet()) {
			generateAllEntitiesInPackage(entry.getKey(), entry.getValue(), processingEnv, roundEnv);
		}
		
		return false;
	}

	public EntityDef makeDef(Element _element) {
		
		TypeElement typeElement = (TypeElement) _element;
		ClassName className = ClassName.get(typeElement);
		
		Element parentElement = findPackage(typeElement);
		if(parentElement != null) {
			HipsterEntity annotation = parentElement.getAnnotation(HipsterEntity.class);
			if(annotation != null) genOptions = new GenOptions(genOptions,annotation);
		}
		
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "annotated class: " + typeElement.getQualifiedName());

		EntityDef def = new EntityDef(typeElement, processingEnv.getElementUtils(), genOptions);
        def.packageElement = parentElement;
		
        for (Element element : typeElement.getEnclosedElements()) {
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
		
		defMap.put(className, def);
		return def;
	}

	private Element findPackage(TypeElement typeElement) {
		Element parentElement = typeElement.getEnclosingElement();
		int i=0;
		while(parentElement != null) {
			if(parentElement.getKind() == ElementKind.PACKAGE) return parentElement;
			parentElement = typeElement.getEnclosingElement();
			if(++i>2) break;
		}
		return null;
	}


	private void generateAllEntitiesInPackage(String packageName, Element packageElement, ProcessingEnvironment processingEnv, RoundEnvironment roundEnv) {
		ClassName className  = ClassName.get(packageName,"AllEntitiesInPackage");
		
		List<EntityDef> list = new ArrayList<>();

		packageElement.accept(new ElementScanner8<Void, Void>() {
			@Override
			public Void visitType(TypeElement e, Void p) {
				HipsterEntity annotation = e.getAnnotation(HipsterEntity.class);
				if(annotation != null) {
					ClassName entityClassName = ClassName.get(e);
					EntityDef def = defMap.get(entityClassName);
					if(def == null) def = makeDef(e);
					
					if(def.genOptions.isGenMeta())
						list.add(def);
				}
				return super.visitType(e, p);
			}
		}, null);
		
		TypeSpec.Builder cp = classBuilder(PUBLIC(),className);

		com.squareup.javapoet.CodeBlock.Builder codeBlock = CodeBlock.builder();
		codeBlock.add("$T.toArray(\n", HipsterSqlUtil.class);
		codeBlock.indent();
		boolean first = true;
		
		for(int i=0; i<list.size(); i++) {
			EntityDef def = list.get(i);
			if(!first) codeBlock.add(",\n");
			codeBlock.add("$T.class", def.typeMeta);
			first = false;
		}
		codeBlock.unindent();
		final CodeBlock code1 = codeBlock.add("\n)").build();
		
		// Class<? extends IEntityMeta>[] ALL_META = HipsterSqlUtil.toArray(...);
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

		first = true;
		for(int i=0; i<list.size(); i++) {
			EntityDef def = list.get(i);
			if(!first) codeBlock.add(",\n");
			codeBlock.add("$T.class", def.type);
			first = false;
		}
		codeBlock.unindent();
		final CodeBlock code2 = codeBlock.add("\n)").build();
		
		// Class<?>[] ALL_ENTITIES = HipsterSqlUtil.toArray(...);
		addField(cp, 
			ArrayTypeName.of(Class.class), 
			"ALL_ENTITIES", 
			field -> {
				field.addModifiers(PUBLIC().STATIC().FINAL().toArray());
				field.initializer(code2);
		} );
		
		write(className,cp.build(),processingEnv);
	}

	private EntityDef generateClass(EntityDef def, ProcessingEnvironment processingEnv) {

        try {
//        	Builder builder = new GenEnum().gen2(def);
//        	write(def.typeEnum.packageName(), builder.build(), processingEnv);
        	
        	String[] className = HipsterProcessorUtil.splitClassName(processingEnv.getOptions().getOrDefault("hipster_proc_column_meta_class",ColumnMeta.class.getName()));
        	ClassName columnMetaBase  = ClassName.get(className[0],className[1]);
        	
        	Builder builder = new GenImmutable(columnMetaBase).gen2(def);
        	write(def.typeImmutable, builder.build(), processingEnv);

        	if(def.genOptions.isGenBuilder()){
        		builder = new GenBuilder(columnMetaBase).gen2(def);
        		write(def.typeBuilder, builder.build(), processingEnv);
        	}

        	if(def.genOptions.isGenUpdate()){
				builder = new GenUpdate(columnMetaBase).gen2(def);
				write(def.typeUpdate, builder.build(), processingEnv);
        	}
        	
    		if(def.genOptions.isGenMeta()){
    			
    			builder = new GenMeta().gen(def,columnMetaBase);
    			JavaFile javaFile = JavaFile.builder(def.typeDelta.packageName(), builder.build())
    					.addStaticImport(HipsterSqlUtil.class,"annotation")
    					.build();
    			write(def.typeDelta.packageName(), javaFile, processingEnv);
    			
//    			builder = new GenDelta().gen2(def);
//    			write(def.typeDelta, builder.build(), processingEnv);

    			builder = new GenVisitor().gen2(def);
    			write(def.typeDelta, builder.build(), processingEnv);
    			
    		}
        	

        	        	
		} catch (Throwable e) {
			processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage()+"\n"+getTrace(e), def.clazz);
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
