package hr.hrg.hipster.processor;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static hr.hrg.hipster.processor.HipsterProcessorUtil.*;
import static hr.hrg.javapoet.PoetUtil.*;

import java.io.*;

import com.squareup.javapoet.*;
import com.squareup.javapoet.MethodSpec.Builder;

import hr.hrg.hipster.entity.*;
import hr.hrg.hipster.jackson.*;
import hr.hrg.hipster.sql.*;

public class GenImmutable {

	private ClassName columnMetaBase;

	public GenImmutable(ClassName columnMetaBase) {
		this.columnMetaBase = columnMetaBase;
	}

	public TypeSpec.Builder gen2(EntityDef def) throws IOException {
		TypeSpec.Builder builder = classBuilder(def.typeImmutable);
		PUBLIC().FINAL().to(builder);
		
		if(def.isInterface){
			builder.addSuperinterface(def.type);			
		}else{
			builder.superclass(def.type);						
		}
		builder.addSuperinterface(IEnumGetter.class);		

        int count = def.getProps().size();
        for(int i=0; i<count; i++) {
        	Property prop = def.getProps().get(i);
        	if(prop.initial != null) {
				String typeStr = prop.type.toString();
        		if(typeStr.equals("java.lang.String"))
        			addField(builder, PRIVATE(), prop.type, prop.name, "$S", prop.initial);
        		else
        			addField(builder, PRIVATE(), prop.type, prop.name, "$L", prop.initial);
        	}else {        		
        		addField(builder, PRIVATE(), prop.type, prop.name);
        	}
        	
			MethodSpec.Builder g = methodBuilder(PUBLIC(), prop.type, prop.getterName).addAnnotation(Override.class);
			copyAnnotations(g, prop);
			g.addCode("return "+prop.fieldName+";\n");
			builder.addMethod(g.build());
			
			if(prop.isTransient()) {
				g = methodBuilder(PUBLIC(), void.class, prop.setterName);
				addSetterParameter(g, prop.type, prop.name, null);
				builder.addMethod(g.build());				
			}
        }
       
        addEnumGetter(def, builder, columnMetaBase);
        addEquals(def, builder);
        genConstructor(def,builder);
        
		if(def.genOptions.isGenJson()) addDirectSerializer(def,builder);
        
		return builder;
	}

	public static void copyAnnotations(MethodSpec.Builder g, Property prop) {
//		if(prop.jsonIgnore) g.addAnnotation(CN_JsonIgnore);
		// TODO allow config to skip some annotations
		for(AnnotationSpec a:prop.annotations) {
			g.addAnnotation(a);
		}
	}

	public static void addDirectSerializer(EntityDef def, TypeSpec.Builder builder){
		builder.addAnnotation(annotationSpec(CN_JsonSerialize, "using", "$T.class", DirectSerializer.class));
		builder.addSuperinterface(IDirectSerializerReady.class);

		addMethod(builder, PUBLIC(), void.class, "serialize", method -> {		
			addParameter(method, CN_JsonGenerator, "jgen");
			addParameter(method, CN_SerializerProvider, "provider");
		
			method.addException(IOException.class);
			method.addException(CN_JsonGenerationException);
			
			method.addCode("jgen.writeStartObject();\n\n");
			
			// TODO more by spec
			if(def.jsonTypeInfo != null) {
				
				if(def.genOptions.isGenMeta())
					method.addCode("jgen.writeStringField(\"@type\", $T.ENTITY_NAME);\n", def.typeMeta);
				else
					method.addCode("jgen.writeStringField(\"@type\", $S);\n", def.entityName);
			}
			
			int count = def.getProps().size();
			for (int i = 0; i < count; i++) {
				Property prop = def.getProps().get(i);
				
				if(prop.jsonIgnore) continue;
				
				String typeStr = prop.type.toString();
				boolean primitive = prop.type.isPrimitive();
				
				if(!prop.isKeepRest()) {
					method.addCode("jgen.writeFieldName($S);\n",prop.name);
				}

				if(primitive || prop.type.isBoxedPrimitive()){
					TypeName unboxed = prop.type.unbox();
					if(TypeName.INT.equals(unboxed) 
							|| TypeName.LONG.equals(unboxed)
							|| TypeName.FLOAT.equals(unboxed)
							|| TypeName.DOUBLE.equals(unboxed)
							|| TypeName.SHORT.equals(unboxed)
							|| TypeName.BYTE.equals(unboxed)
							){
						addWrite(method, "writeNumber", prop.name, !primitive);
					}else if(TypeName.BOOLEAN.equals(unboxed)){
						addWrite(method, "writeBoolean", prop.name, !primitive);						
					}else if(TypeName.CHAR.equals(unboxed)){
						if(!primitive){
							method.addCode("if ($L == null)\n",prop.name);
							method.addCode("\tjgen.writeNull();\n");
							method.addCode("else\n");
							method.addCode("\t");
						}
						method.addCode("jgen.writeString(new char[]{$L},0,1);\n",prop.name);
					}
				} else if(typeStr.equals("java.lang.String")){
					addWrite(method, "writeString", prop.name, true);
				} else if(prop.isKeepRest()){
					if(prop.type.toString().equals("java.util.Map")) {
						method.addCode("$T.writeKeepMap($L,jgen,provider);\n",JacksonUtil.class,prop.name);
					}else {
						method.addCode("$T.writeKeepObjectNode($L, jgen, provider);\n",JacksonUtil.class,prop.name);						
					}
				} else {
					addWrite(method, "writeObject", prop.name, true);
				}
				method.addCode("\n");
			}
			
			method.addCode("jgen.writeEndObject();\n");
			
		});
	}

	public static void addWrite(Builder method, String jgenMethod, String prop, boolean nullCheck) {
		if(!nullCheck)
			method.addCode("jgen.$L($L);\n",jgenMethod,prop);
		else{
			method.addCode("if ($L == null)\n",prop);
			method.addCode("\tjgen.writeNull();\n");
			method.addCode("else\n");
			method.addCode("\tjgen.$L($L);\n",jgenMethod,prop);
		}
	}

	public static void addEquals(EntityDef def, TypeSpec.Builder cp){
		Property primaryProp = def.getPrimaryProp();
		if(primaryProp == null) return;
		
		addMethod(cp, PUBLIC(), boolean.class, "equals", method -> {
			method.addAnnotation(Override.class);
			addParameter(method, Object.class, "o");
			method.addCode("if(o == null || !(o instanceof $T)) return false;\n", def.type);
			method.addCode("return $L.equals((($T)o).$L());\n", primaryProp.name, def.type, primaryProp.getterName);
		});
		
	}
	public static void addEnumGetter(EntityDef def, TypeSpec.Builder cp,ClassName columnMetaBase){

		TypeVariableName typeT = TypeVariableName.get("T");
		TypeVariableName typeE = TypeVariableName.get("E", parametrized(Key.class, typeT));
		
		addMethod(cp, typeT, "getValue", method -> {
			method.addTypeVariable(typeT);
			method.addTypeVariable(typeE);
			PUBLIC().FINAL().to(method);
	        method.addAnnotation(Override.class);
	        addParameter(method, typeE	, "column");
	        method.addCode("return ($T)this.getValue(column.ordinal());\n", typeT);
		});
		
		addMethod(cp, Object.class, "getValue", method -> {
			PUBLIC().FINAL().to(method);
			method.addAnnotation(Override.class);
			addParameter(method, int.class, "_ordinal");
			method.addCode("switch (_ordinal) {\n");

			int count = def.getProps().size();
			for (int i = 0; i < count; i++) {
				Property prop = def.getProps().get(i);
				if(prop.isTransient()) continue;
				method.addCode("case " + i + ": return this." + prop.fieldName + ";\n");

			}
			method.addCode("default: throw new ArrayIndexOutOfBoundsException(_ordinal);\n");
			method.addCode("}\n");
		});
	}

	public static void genConstructor(EntityDef def, TypeSpec.Builder cp){

		MethodSpec.Builder constr0 = constructorBuilder(PUBLIC());
		
        MethodSpec.Builder constr = constructorBuilder(PUBLIC());
        if(def.genOptions.isGenJson()) constr.addAnnotation(CN_JsonCreator);
		
        MethodSpec.Builder constr2 = constructorBuilder(PUBLIC());
        addParameter(constr2,def.type, "v");
        
        constr2.addCode("if(v == null) return;\n");
        
        int count = def.getProps().size();
        for(int i=0; i<count; i++) {
        	Property property = def.getProps().get(i);

        	if(!property.isTransient()) {
        		addSetterParameter(constr, property.type, property.name, param->{
        			if(def.genOptions.isGenJson())
        				param.addAnnotation(annotationSpec(CN_JsonProperty,"value", "$S",property.name));
        		});
        		constr2.addCode("this."+property.name+" = v."+property.getterName +"();\n");
        	}
        }
        cp.addMethod(constr0.build());
        cp.addMethod(constr.build());
        cp.addMethod(constr2.build());
	}
	
}
