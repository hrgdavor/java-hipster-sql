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

import hr.hrg.hipster.entity.*;
import hr.hrg.hipster.sql.*;

@SupportedAnnotationTypes("hr.hrg.hipster.entity.HipsterEntity")
@SupportedOptions({"hipster_proc_jackson","hipster_proc_builder", "hipster_proc_column_meta_class"})
public class HipsterDaoProcessor extends AbstractProcessor{

	private static Map<String, List<ClassName>> packageClasses = new HashMap<String, List<ClassName>>();
	private static Map<String, List<ClassName>> packageMetas = new HashMap<String, List<ClassName>>();
	
	static int counter= 0;
	
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
    }
    
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(HipsterEntity.class);
		List<EntityDef> defs = new ArrayList<EntityDef>();
		Map<String,List<EntityDef>> defMap = new HashMap<>();

   		boolean jackson = "true".equalsIgnoreCase(processingEnv.getOptions().get("hipster_proc_jackson"));
   		boolean genBuilder = "true".equalsIgnoreCase(processingEnv.getOptions().get("hipster_proc_builder"));
   		boolean genVisitor = "true".equalsIgnoreCase(processingEnv.getOptions().get("hipster_proc_visitor"));
   		boolean genUpdate = "true".equalsIgnoreCase(processingEnv.getOptions().get("hipster_proc_update"));

   		GenOptions genOptions = new GenOptions(jackson,true,genVisitor,genUpdate, genBuilder);
        
		
		processingEnv.getMessager().printMessage(Kind.NOTE, "process classes "+elements);
		for (Element element : elements) {
			if		(element.getKind() == ElementKind.INTERFACE) {
				GenOptions packageGenOptions = genOptions;
				
				TypeElement typeElement = (TypeElement) element;
				Element parentElement = findPackage(typeElement);
				if(parentElement != null) {
					HipsterEntity annotation = parentElement.getAnnotation(HipsterEntity.class);
					if(annotation != null) packageGenOptions = new GenOptions(genOptions,annotation);
				}
				
				EntityDef def = generateClass(typeElement, processingEnv, packageGenOptions);
				defs.add(def);
				List<EntityDef> list = defMap.get(def.packageName);
				if(list == null) {
					list = new ArrayList<>();
					defMap.put(def.packageName, list);
				}
				list.add(def);
				
				addClassName(packageClasses, def.packageName,def.type);
				if(def.genOptions.isGenMeta()) addClassName(packageMetas, def.packageName,def.typeMeta);
				
				
			}else{
				processingEnv.getMessager().printMessage(Kind.NOTE, "skip because not interface "+element);				
			}
		}
		
		for(Entry<String, List<EntityDef>> entry:defMap.entrySet()) {
			generateAllEntitiesInPackage(entry.getKey(), entry.getValue(), processingEnv, roundEnv);
		}
		
		return false;
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

	private void addClassName(Map<String, List<ClassName>> map, String packageName, ClassName type) {
		List<ClassName> list = map.get(packageName);
		if(list == null) {
			list = new ArrayList<ClassName>();
			map.put(packageName, list);
		}
		if(!list.contains(type)) {
			list.add(type);
		}
	}

	private void generateAllEntitiesInPackage(String packageName, List<EntityDef> defs, ProcessingEnvironment processingEnv, RoundEnvironment roundEnv) {
		ClassName className  = ClassName.get(packageName,"AllEntitiesInPackage");
		
//		EntityDef entityDef0 = defs.get(0);
//
//		PackageElement packageElement = processingEnv.getElementUtils().getPackageElement(packageName);
//		processingEnv.getMessager().printMessage(Kind.NOTE, "package all: "+packageName+" "+packageElement == null ? "null":packageElement.toString());
//
//		if(packageElement == null) {
//			processingEnv.getMessager().printMessage(Kind.NOTE, "skipping package: "+packageName);
//			System.err.println("skipping package "+packageName);
//			return;
//		}
//		System.err.println("package: "+packageName+" "+packageElement);
//		List<? extends Element> enclosedElements = packageElement.getEnclosedElements();
//		
//		defs = new ArrayList<EntityDef>();
//		for(Element element:enclosedElements) {
//			processingEnv.getMessager().printMessage(Kind.NOTE, "package element add: "+element.getSimpleName());
//			if(element.getKind() == ElementKind.INTERFACE && element.getAnnotation(HipsterEntity.class) != null) {
//				EntityDef def = new EntityDef((TypeElement)element, processingEnv.getElementUtils());
//				defs.add(def);
//			}
//		}
		
		TypeSpec.Builder cp = classBuilder(PUBLIC(),className);

		com.squareup.javapoet.CodeBlock.Builder codeBlock = CodeBlock.builder();
		codeBlock.add("$T.toArray(\n", HipsterSqlUtil.class);
		codeBlock.indent();
		boolean first = true;
//		for(int i=0; i<defs.size(); i++) {
//			EntityDef entityDef = defs.get(i);
//			if(entityDef.genMeta) {				
//				if(!first) codeBlock.add(",\n");
//				codeBlock.add("$T.class", entityDef.typeMeta);
//				first = false;
//			}
//		}
		List<ClassName> list = packageMetas.get(packageName);
		if(list != null) for(int i=0; i<list.size(); i++) {
			ClassName cName = list.get(i);
			if(!first) codeBlock.add(",\n");
			codeBlock.add("$T.class", cName);
			first = false;
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
//		for(int i=0; i<defs.size(); i++) {
//			codeBlock.add("$T.class", defs.get(i).type);
//			if(i != defs.size()-1) codeBlock.add(",\n");
//		}
		first = true;
		list = packageClasses.get(packageName);
		for(int i=0; i<list.size(); i++) {
			ClassName cName = list.get(i);
			if(!first) codeBlock.add(",\n");
			codeBlock.add("$T.class", cName);
			first = false;
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

	private EntityDef generateClass(TypeElement clazz, ProcessingEnvironment processingEnv, GenOptions genOptions) {
		        

        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "annotated class: " + clazz.getQualifiedName());
        
        EntityDef def = new EntityDef(clazz, processingEnv.getElementUtils(), genOptions);
        
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
