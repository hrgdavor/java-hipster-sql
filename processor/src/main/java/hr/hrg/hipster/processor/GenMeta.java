package hr.hrg.hipster.processor;

import static hr.hrg.hipster.processor.HipsterProcessorUtil.*;
import static hr.hrg.javapoet.PoetUtil.*;

import java.sql.*;
import java.util.*;
import java.util.Map.*;

import javax.annotation.processing.*;
import javax.tools.*;

import org.bson.*;
import org.bson.codecs.*;
import org.bson.codecs.configuration.*;

import com.squareup.javapoet.*;
import com.squareup.javapoet.MethodSpec.*;

import hr.hrg.hipster.entity.*;
import hr.hrg.hipster.jackson.*;
import hr.hrg.hipster.sql.*;
import hr.hrg.hipster.type.*;
import hr.hrg.javapoet.*;


public class GenMeta {

	public TypeSpec.Builder gen(EntityDef def, ClassName columnMetaBase, ProcessingEnvironment processingEnv) {
		
		TypeSpec.Builder cp = classBuilder(PUBLIC(), def.typeMeta);

		addComments(cp, def, columnMetaBase);
		
		Property primaryProp = def.getPrimaryProp();
		TypeName primaryType = primaryProp == null ? TypeName.get(Object.class) : primaryProp.type;
	
		Class<?> entityMetaClass = def.genOptions.isGenMongo() ? MongoEntityMeta.class : EntityMeta.class;
		cp.superclass(parametrized(entityMetaClass,def.type, primaryType, columnMetaBase, def.genOptions.isGenVisitor() ? def.typeVisitor : TypeName.OBJECT));
		if(def.genOptions.isGenJson()) {
			addField(cp, PRIVATE(), CN_ObjectMapper,"mapper");	
			cp.addSuperinterface(WithMapper.class);
			
			addMethod(cp,PUBLIC().FINAL(), CN_ObjectMapper, "getMapper", method->{
				method.addAnnotation(Override.class);
				method.addCode("return mapper;\n");
			});			

			addMethod(cp,PUBLIC().FINAL(), void.class, "setMapper", method->{
				method.addAnnotation(Override.class);
				addParameter(method, CN_ObjectMapper, "v");
				method.addCode("this.mapper = v;\n");
			});			
		}
		// public static final Class<SampleEntity> ENTITY_CLASS = SampleEntity.class;
		addField(cp, PUBLIC().STATIC().FINAL(), parametrized(Class.class, def.type), "ENTITY_CLASS", "$T.class",def.type);	
		addField(cp, PUBLIC().STATIC().FINAL(), String.class, "ENTITY_NAME", "$S",def.entityName);	

		int ordinal = 0;
		for(Property prop: def.getProps()){
			if(prop.isTransient()) continue;
			// type or raw type
			TypeName rawType = prop.type;
			if(prop.type instanceof ParameterizedTypeName){				
				ParameterizedTypeName parameterizedTypeName = (ParameterizedTypeName)prop.type;
				rawType = parameterizedTypeName.rawType;
			}

			addField(cp, PUBLIC().FINAL(), parametrized(columnMetaBase, rawType.box()), prop.fieldName, field -> field.addJavadoc("ordinal: $L", prop.ordinal));	
			ordinal++;
		}

//		if(primaryProp == null) {
//			addField(cp, PUBLIC().STATIC().FINAL(), columnMetaBase, "PRIMARY", "null");
//			
//		}else {
//			addField(cp, PUBLIC().STATIC().FINAL(), parametrized(columnMetaBase, def.getPrimaryProp().type), "PRIMARY", primaryProp.name);			
//		}
				
		
		FieldSpec ordinalField = addField(cp,PRIVATE().FINAL(), int.class, "_ordinal");
		
//		for(Property p:def.getProps()) {
//			if(getterName(p) == null){
//				addField(cp,PUBLIC().FINAL(), parametrized(ICustomType.class, p.type), "_"+p.fieldName+"_typeHandler");
//			}
//		}

		MethodSpec.Builder constr = constructorBuilder(PUBLIC());
		addParameter(constr, HipsterSql.class, "hipster");
		constr.addCode("super(_ordinal, $S, ENTITY_CLASS, hipster);\n", def.tableName);
		addSetterParameter(constr, ordinalField, null);
		int i=0;
		
		
		if(def.genOptions.isGenMongo()) constr.addCode("_codecs = new $T<?>[COLUMN_COUNT];\n", Codec.class);
		
		if(def.genOptions.isGenSql()) {			
			constr.addCode("_typeHandler = new $T<?>[COLUMN_COUNT];\n", ICustomType.class);

			CodeBlock.Builder typeHandlersBlock = CodeBlock.builder();
			typeHandlersBlock.add("if(hipster != null){\n");
			typeHandlersBlock.indent();
			typeHandlersBlock.add("$T _typeSource = hipster.getTypeSource();\n", TypeSource.class);
			boolean hasGetters = false;
			
			for(Property p:def.getProps()) {
				if(p.isTransient()) continue;
				ParameterizedTypeName parametrizedCustomType = parametrized(ICustomType.class, p.type.box());

				if(!p.customTypeKey.isEmpty()){
					typeHandlersBlock.add("_typeHandler["+i+"] = ($T) _typeSource.getNamedRequired($S);", parametrizedCustomType, p.customTypeKey);
				}else if(p.customType != null) {
					typeHandlersBlock.add("_typeHandler["+i+"] = ($T) _typeSource.getInstanceRequired($T.class);", parametrizedCustomType, p.customType);				
				}else {				
					typeHandlersBlock.add("_typeHandler["+i+"] = ($T) _typeSource.getForRequired(", parametrizedCustomType);
					hasGetters = true;
					if(p.type instanceof ParameterizedTypeName){
						ParameterizedTypeName parameterizedTypeName = (ParameterizedTypeName)p.type;
						typeHandlersBlock.add("$T.class",parameterizedTypeName.rawType);
						for(TypeName ta: parameterizedTypeName.typeArguments){
							typeHandlersBlock.add(",$T.class",ta);					
						}
						typeHandlersBlock.add(");");
					}else{
						typeHandlersBlock.add("$T.class);", p.type.box());
					}
				}
				typeHandlersBlock.add("// $L \n", p.fieldName);
				
				i++;
			}
			
			typeHandlersBlock.unindent();
			typeHandlersBlock.add("}\n");
			
			constr.addCode(typeHandlersBlock.build());
		}

		addColumnsDef(cp, constr, def, columnMetaBase);

		if(def.keepRestPop != null) constr.addCode("_keepRestColumn = $L;\n", def.keepRestPop.fieldName);
		
//		addconstructor(cp, PUBLIC(), method-> {
//			method.addParameter(HipsterSql.class, "hipster");
//			method.addCode("this(hipster.getGetterSource());\n");
//			method.addCode("hipster.getReaderSource().registerFor(this, $T.class);\n",def.type);
//		});
		
		cp.addMethod(constr.build());
		
		if(def.genOptions.isGenSql()) add_fromResultSet(cp, def);

//		TypeName returnType = parametrized(EnumArrayUpdateDelta.class,columnMetaBase);
//		addMethod(cp,PRIVATE().STATIC().FINAL(), returnType, "delta", delta->{
//			delta.addParameter(long.class, "changeSet");
//			delta.addParameter(ArrayTypeName.of(Object.class), "values");
//			delta.addCode("return new $T(changeSet, values, $T.COLUMN_ARRAY);\n", returnType,columnMetaBase);			
//		});
		
		
		//@Override
		//public final SampleImmutable immutableCopy(Sample v){ 
		//  if(v instanceof SampleImmutable) return (SampleImmutable)v;
		//  SapmleImmutable out = new SapmleImmutable((Sample)v); 
		//  this.init(out)
		//  return out;
		//}
		addMethod(cp,PUBLIC().FINAL(), def.typeImmutable, "immutableCopy", method->{
			method.addAnnotation(Override.class);
			addParameter(method, def.type, "v");
			method.addCode("if(v instanceof $T) return ($T)v;\n", def.typeImmutable, def.typeImmutable);
			method.addCode("$T out = new $T(($T)v);\n", def.typeImmutable, def.typeImmutable, def.type);
			method.addCode("this.init(out);\n");
			method.addCode("return out;\n");
		});

		//@Override
		//public final boolean isImmutableVariant(Sample v){ return v instanceof SampleImmutable; }
		addMethod(cp,PUBLIC().FINAL(), boolean.class, "isImmutableVariant", method->{
			method.addAnnotation(Override.class);
			addParameter(method, def.type, "v");
			method.addCode("return v instanceof $T;\n", def.typeImmutable);
		});
		
		if(def.genOptions.isGenUpdate()){			
			//@Override
			//public final SampleUpdate mutableCopy(Sample v){ return new SmapleUpdate(v); }
			addMethod(cp,PUBLIC().FINAL(), def.typeUpdate, "mutableCopy", method->{
				method.addAnnotation(Override.class);
				addParameter(method, def.type, "v");
				method.addCode("return new $T(v);\n", def.typeUpdate);
			});
		}else{
			//@Override
			//public final IUpdatable<ColumnMeta> mutableCopy(Sample v){ throw new RuntimeExcep(v); }
			addMethod(cp,PUBLIC().FINAL(), IUpdatable.class, "mutableCopy", method->{
				method.addAnnotation(Override.class);
				addParameter(method, def.type, "v");
				method.addCode("throw new $T($S);\n", RuntimeException.class,"can not be implemented without updater");
			});
			
		}

		//@Override
//		public final Class[] getImplClasses(){ return new Class[]{}; }
		addMethod(cp,PUBLIC().FINAL(), ArrayTypeName.of(Class.class), "getImplClasses", method->{
			method.addAnnotation(Override.class);
			method.addCode("return new Class[]{\n");
			if(def.genOptions.isGenUpdate())  method.addCode("$T.class,\n", def.typeUpdate);
			method.addCode("$T.class\n", def.typeImmutable);
			method.addCode("};\n");
		});

//		public final Class getImmutableClass(){  }
		addMethod(cp,PUBLIC().FINAL(), Class.class, "getImmutableClass", method->{
			method.addAnnotation(Override.class);
			method.addCode("return $T.class;\n",def.typeImmutable);
		});

		if(def.genOptions.isGenUpdate()) {			
//		public final Class getUpdateClass(){  }
			addMethod(cp,PUBLIC().FINAL(), Class.class, "getUpdateClass", method->{
				method.addAnnotation(Override.class);
				method.addCode("return $T.class;\n",def.typeUpdate);
			});
		}
		
		//@Override
		//public final String getEntityName(){ return "Sample"; }
		addMethod(cp,PUBLIC().FINAL(), String.class, "getEntityName", method->{
			method.addAnnotation(Override.class);
			method.addCode("return $S;\n", def.simpleName);
		});
		
		//@Override
		//public final String getColumnNamesStr(){ return EntityEnum.COLUMNS_STR; }
//		addMethod(cp,PUBLIC().FINAL(), String.class, "getColumnNamesStr", method->{
//			method.addAnnotation(Override.class);
//			method.addCode("return COLUMNS_STR;\n");
//		});

		//public final String getColumnNames(){ return EntityEnum.COLUMN_NAMES; }
//		addMethod(cp,PUBLIC().FINAL(), parametrized(ImmutableList.class,String.class), "getColumnNames", method->{
//			method.addCode("return COLUMN_NAMES;\n");
//		});		

		//@Override
		//public final boolean containsColumn(){ return EntityEnum.COLUMN_NAMES.contains(columnName); }
		addMethod(cp,PUBLIC().FINAL(), boolean.class, "containsColumn", method->{
			addParameter(method, String.class, "columnName");
			method.addCode("return $T.binarySearch(COLUMN_ARRAY_SORTED_STR, columnName) > -1;\n",Arrays.class);
		});		

		//@Override
		//public final boolean containsColumn(){ return EntityEnum.COLUMN_NAMES.contains(columnName); }
		addMethod(cp,PUBLIC().FINAL(), boolean.class, "containsField", method->{
			addParameter(method, String.class, "columnName");
			method.addCode("return $T.binarySearch(FIELD_ARRAY_SORTED_STR, columnName) > -1;\n",Arrays.class);
		});		
		
		//@Override
		//public final String getColumns(){ return _columns; }
//		addMethod(cp,PUBLIC().FINAL(), parametrized(ImmutableList.class,columnMetaBase), "getColumns", method->{
//			method.addAnnotation(Override.class);
//			method.addCode("return _columns;\n");
//		});		
		//@Override
		//public final String getPrimaryColumn(){ return COLUMNS_STR; }
		addMethod(cp,PUBLIC().FINAL(), columnMetaBase, "getPrimaryColumn", method->{
			method.addAnnotation(Override.class);
			method.addCode("return "+(primaryProp == null ? "null":primaryProp.fieldName)+";\n");
		});

		//@Override
		//public final SampleEnum getColumn(String name){ return SamleEnum.valueOf(name); }
//		addMethod(cp,PUBLIC().FINAL(), columnMetaBase, "getColumn", method->{
//			method.addParameter(String.class, "columnName");
//			method.addAnnotation(Override.class);
//			method.addCode("int pos = $T.binarySearch(COLUMN_ARRAY_SORTED_STR, columnName);\n",Arrays.class);
//			method.addCode("return pos < 0 ? null: COLUMN_ARRAY_SORTED[pos];\n");
//		});
		
		//@Override
		//public final SampleEnum getColumn(String name){ return COLUMN_ARRAY[_ordinal]; }
//		addMethod(cp,PUBLIC().FINAL(), columnMetaBase, "getColumn", method->{
//			method.addParameter(int.class, "_ordinal");
//			method.addAnnotation(Override.class);
//			method.addCode("return COLUMN_ARRAY[_ordinal];\n");
//		});	
		
		if(primaryProp == null){
			//@Override
			//public final Object entityGetPrimary(Sample instance){ return null; }
			addMethod(cp,PUBLIC().FINAL(), Object.class, "entityGetPrimary", method->{
				method.addParameter(def.type, "instance");
				method.addAnnotation(Override.class);
				method.addCode("return null;\n");
			});
		}else{
			//@Override
			//public final Long entityGetPrimary(Sample instance){ return instance.getId(); }
			addMethod(cp,PUBLIC().FINAL(), primaryProp.type, "entityGetPrimary", method->{
				method.addParameter(def.type, "instance");
				method.addAnnotation(Override.class);
				method.addCode("return instance."+primaryProp.getterName+"();\n");
			});
		}
		

		if(def.genOptions.isGenMongo()) {
			addMethod(cp,PUBLIC().FINAL(), parametrized(Class.class, def.type), "getEncoderClass", method->{
				method.addAnnotation(Override.class);
				method.addCode("return $L.class;\n", def.simpleName);
			});
			
			addMethod(cp,PUBLIC().FINAL(), void.class, "setCodecRegistry", method->{
				method.addAnnotation(Override.class);
				method.addParameter(CodecRegistry.class, "registry");
				method.addCode("super.setCodecRegistry(registry);\n\n", def.simpleName);
				
				for(Property p: def.getProps()) {
					if(p.isTransient()) continue;

					TypeName type = isList(p.type) ? p.componentType : p.type;
					if(type instanceof ParameterizedTypeName) {
						type = ((ParameterizedTypeName)type).rawType;
					}
					String getterNameMongo = getterNameMongo(type.toString());
//					if(getterNameMongo == null) {
					if(!type.isPrimitive()) {
						method.addCode("_codecs[$L] = registry.get($T.class);\n",p.ordinal, type);
					}
				}
			});
			
			add_decode_mongo(cp, def, processingEnv);
			
			add_encode_mongo(cp, def, processingEnv);
			
		}
		
		return cp;
	}
	
	private void addComments(com.squareup.javapoet.TypeSpec.Builder cp, EntityDef def, ClassName columnMetaBase) {
		String typeDao = def.type.simpleName()+"Dao";
		cp.addJavadoc("Example meta from EntitySource:\n");
		cp.addJavadoc("<pre>\n");
		cp.addJavadoc("$T meta = ($T) hip.getEntitySource().getFor($T.class);", def.typeMeta, def.typeMeta, def.type);
		cp.addJavadoc("</pre>\n");
		cp.addJavadoc("\n");
		
		cp.addJavadoc("Example dao class with proper generic arguments:\n");
		cp.addJavadoc("<pre>\n");
		cp.addJavadoc("import hr.hrg.hipster.entity.*;\n");
		cp.addJavadoc("import hr.hrg.hipster.sql.*;\n");
		cp.addJavadoc("import $L.*;\n", def.packageName);
		cp.addJavadoc("\n");
		Object primaryClass = def.getPrimaryProp() == null ? Object.class : def.getPrimaryProp().type;
		cp.addJavadoc("public class $L extends EntityDao&lt;$T, $T, $T, $T&gt;{\n",typeDao, def.type, primaryClass, columnMetaBase, def.typeMeta);
		cp.addJavadoc("\n");
		cp.addJavadoc("	public $L(IHipsterConnection conn) {\n", typeDao);
		cp.addJavadoc("\n");
		cp.addJavadoc("		super($T.class, conn);\n",def.type);
		cp.addJavadoc("	}\n");
		cp.addJavadoc("}\n");
		cp.addJavadoc("</pre>\n");
		cp.addJavadoc("\n");

		String typeCache = def.type.simpleName()+"Cache";
		cp.addJavadoc("Example cache class with proper generic arguments:\n");
		cp.addJavadoc("<pre>\n");
		cp.addJavadoc("import hr.hrg.hipster.entity.*;\n");
		cp.addJavadoc("import hr.hrg.hipster.sql.*;\n");
		cp.addJavadoc("import $L.*;\n", def.packageName);
		cp.addJavadoc("\n");
		cp.addJavadoc("public class $L extends SimpleEntityCache&lt;$T, $T, $T&gt;{\n",typeCache, def.type, primaryClass, def.typeMeta);
		cp.addJavadoc("\n");
		cp.addJavadoc("	public $L(HipsterSql hipster) {\n", typeCache);
		cp.addJavadoc("\n");
		cp.addJavadoc("		super(hipster.getEventHub(), ($T)hipster.getEntitySource().getFor($T.class));\n",def.typeMeta,def.type);
		cp.addJavadoc("	}\n");
		cp.addJavadoc("}\n");
		cp.addJavadoc("</pre>\n");
		cp.addJavadoc("\n");

		cp.addJavadoc("Example listener:\n");
		cp.addJavadoc("<pre>\n");
		cp.addJavadoc("hip.getEventHub().addListener($T.class,(IEntityEventListener&lt;$T, $T, $T&gt;)\n", def.type, def.type, primaryClass, def.typeMeta);
		cp.addJavadoc("		(type, id, old, updated, delta, meta, batchId) -> {\n");
		cp.addJavadoc("	// your code here\n");
		cp.addJavadoc("}, EntityEventType.AFTER_CHANGE, EntityEventType.AFTER_ADD);// if no types are provided here, then it is subscription for all types \n");
		cp.addJavadoc("</pre>\n");
		cp.addJavadoc("\n");
		
		if(def.genOptions.isGenVisitor()) {			
			cp.addJavadoc("Example visitor lambda:\n");
			cp.addJavadoc("<pre>\n");
			cp.addJavadoc("meta.visitResults(hc, query, (");
			int i=1;
			for(Property p:def.getProps()) {
				if(p.isTransient() || p.isKeepRest()) continue;
				if(i>1) cp.addJavadoc(", ");
				cp.addJavadoc(p.fieldName);
				i++;
			}
			cp.addJavadoc(") -> {\n");
			cp.addJavadoc("	// your code here\n");
			cp.addJavadoc("});\n");
			cp.addJavadoc("</pre>\n");
			cp.addJavadoc("\n");
		}
		

	}

	private String getterName(Property p){
		if(isType(p, "int","java.lang.Integer")){
			return "getInt";
		
		}else if(isType(p, "boolean","java.lang.Boolean")){
			return "getBoolean";
		
		}else if(isType(p, "long","java.lang.Long")){
			return "getLong";
			
		}else if(isType(p, "double","java.lang.Double")){
			return "getDouble";
			
		}else if(isType(p, "short","java.lang.Short")){
			return "getShort";
			
		}else if(isType(p, "float","java.lang.Float")){
			return "getFloat";

		}else if(isType(p, "java.lang.String")){
			return "getString";
		}

		return null;
	}

	private String getterNameMongo(String typeName){
		if("int".equals(typeName)) return "decodeInt";
		if("java.lang.Integer".equals(typeName)) return "decodeIntObject";
		
		if("boolean".equals(typeName)) return "decodeBoolean";
		if("java.lang.Boolean".equals(typeName)) return "decodeBooleanObject";
		
		if("long".equals(typeName)) return "decodeLong";
		if("java.lang.Long".equals(typeName)) return "decodeLongObject";

		if("float".equals(typeName)) return "decodeFloat";
		if("java.lang.Float".equals(typeName)) return "decodeFloatObject";
		
		if("double".equals(typeName)) return "decodeDouble";
		if("java.lang.Double".equals(typeName)) return "decodeDoubleObject";
		
		if("short".equals(typeName)) return "decodeShort";
		if("java.lang.Short".equals(typeName)) return "decodeShortObject";
		
		if("java.lang.String".equals(typeName)) return "decodeString";
		
		return null;
	}
	
	private void add_decode_mongo(TypeSpec.Builder cp, EntityDef def, ProcessingEnvironment processingEnv) {
		
		MethodSpec.Builder method = methodBuilder(PUBLIC().FINAL(), def.type, "decode");
		method.addAnnotation(Override.class);
		
		method.addParameter(BsonReader.class, "reader");
		method.addParameter(DecoderContext.class, "decoderContext");

		method.addCode("if(reader.getCurrentBsonType() == BsonType.NULL){ reader.readNull(); return null;}\n");
		method.addCode("\n");
		
		CodeBlock.Builder returnValue = CodeBlock.builder().add("$T out = new $T(",def.typeImmutable,def.typeImmutable);
		int i=0;
		for(Property p: def.getProps()) {
			if(p.isTransient()) continue;
			
			method.addCode("$T $L",p.type, p.fieldName);
			if(p.initial != null) {				
				String typeStr = p.type.toString();
        		if(typeStr.equals("java.lang.String")) {
        			method.addCode(" = $S;\n", p.initial);        			
        		}else {
        			method.addCode(" = $L;\n", p.initial);
        		}
			}else if(p.isPrimitive()) {
				TypeName unboxed = p.type.unbox();
				if(TypeName.BOOLEAN.equals(unboxed)) {					
					method.addCode(" = false;\n");
				}else {
					method.addCode(" = 0;\n");					
				}
			}else {
				method.addCode(" = null;\n");				
			}

			if(i>0) returnValue.add(", ");
			returnValue.add(p.fieldName);
			i++;
		}

		String getColumn = def.genOptions.isMongoUseFieldName() ?  "getField":"getColumn";
		method.addCode("\n");
		method.addCode("reader.readStartDocument();\n");
		method.addCode("String fieldName = null;\n");
		method.addCode("try {\n");
		method.addCode("while (reader.readBsonType() != $T.END_OF_DOCUMENT) {\n",BsonType.class);
		method.addCode("fieldName = reader.readName();\n");
		method.addCode("\t$T<?> column = $L(fieldName);// we consider mongo database, so field name is used\n", ColumnMeta.class, getColumn);
		method.addCode("\tif(column == null && \"_id\".equals(fieldName)) column = $L(\"id\");// check _id\n", getColumn);
//		method.addCode("if(column == null && \"_id\".equals(fieldName)) column = $L(\"id\");\n", getColumn);
		method.addCode("\n");
		method.addCode("\tif(column != null) {\n");
		method.addCode("\t\tswitch (column.ordinal()) {\n");
		for(Property p: def.getProps()) {
			if(p.isTransient()) continue;
			
			String typeName = p.type.toString();
			String getterNameMongo = getterNameMongo(typeName);
			method.addCode("\t\t\tcase $L: ", p.ordinal);
//			if(getterNameMongo == null && p.componentType != null) {
//				processingEnv.getMessager().printMessage(
//						Diagnostic.Kind.NOTE,
//						"isList "+isList(p.type)+" "+def.entityName+"."+p.name+":" + typeName);
//				
//			}
			if(getterNameMongo != null) {
				method.addCode("$L = $T.$L(reader);",p.fieldName, MongoDecode.class, getterNameMongo);
			}else if(p.componentType != null && (p.array || isList(p.type))) {
				TypeName type = p.componentType;
				boolean primitive = type.isBoxedPrimitive();
				TypeName unboxed = primitive ? type.unbox(): type;
				String typeInnerName = p.componentType.toString();

				String decodeMethod = "";
				if(TypeName.INT.equals(unboxed)) {
					decodeMethod = "Integer";
				}else if(TypeName.LONG.equals(unboxed)) {
					decodeMethod = "Long";
				}else if(TypeName.FLOAT.equals(unboxed)) {
					decodeMethod = "Float";
				}else if(TypeName.DOUBLE.equals(unboxed)) {
					decodeMethod = "Double";
				}else if(TypeName.SHORT.equals(unboxed)) {
					decodeMethod = "Short";
				}else if(TypeName.BOOLEAN.equals(unboxed)) {
					decodeMethod = "Boolean";
				}else if(typeInnerName.equals("String")) {
					decodeMethod = "String";
				}else if(typeInnerName.equals("java.lang.String")) {
					decodeMethod = "String";
				}
				
				boolean withCodec = decodeMethod.isEmpty();
				if(p.array) {
					decodeMethod = "decodeArray"+decodeMethod;
				}else if(isList(typeName)){
					decodeMethod = "decodeList"+decodeMethod;
				}
				if(withCodec) {
					if(typeName.startsWith("hr.hrg.hipster.sql.ImmutableList")) {
						method.addCode("$L = $T.$L(($T<$T>)_codecs[$L], reader, decoderContext);",
								p.fieldName, MongoDecode.class, "decodeListImmutable", Codec.class, p.componentType,p.ordinal);						
					}else {
						method.addCode("$L = $T.$L(($T<$T>)_codecs[$L], reader, decoderContext);/* $L */",
								p.fieldName, MongoDecode.class, decodeMethod, Codec.class, p.componentType,p.ordinal,typeName);									
					}
				}else {
					if(type.isPrimitive()) decodeMethod += "Primitive";
					method.addCode("$L = $T.$L(reader, decoderContext);",
							p.fieldName, MongoDecode.class, decodeMethod);									
				}
			}else {
				method.addCode("$L = (($T<$T>)_codecs[$L]).decode(reader, decoderContext);",p.fieldName,Codec.class, p.type,p.ordinal);				
			}
			method.addCode("break;\n");
		}
		method.addCode("\n");
		method.addCode("\t\t\tdefault: reader.skipValue();\n");
		method.addCode("\t\t}\n");

		
		
		method.addCode("\t}else{\n");
		method.addCode("\t\treader.skipValue();\n");
		method.addCode("\t}\n");
		
		method.addCode("\n");
		method.addCode("}\n");// end while loop
		
		method.addCode("reader.readEndDocument();\n");
				
		method.addCode("}catch(Exception e){\n");
		String debugFieldName = def.getProps().get(0).fieldName;
		if(def.getPrimaryProp() != null) debugFieldName = def.getPrimaryProp().fieldName; 
		method.addCode("\tthrow new RuntimeException(\"Error reading column \"+fieldName+\" \"+ENTITY_CLASS+\"#\"+$L,e);\n", debugFieldName);
		method.addCode("}\n");
	
		method.addCode("try{\n");
		returnValue.add(");\n");
		returnValue.add("this.init(out);\n");
		returnValue.add("return out;\n");
		method.addCode(returnValue.build());
		method.addCode("}catch(Exception e){\n");
		method.addCode("\tthrow new RuntimeException(\"Error initializing \"+ENTITY_CLASS+\"#\"+$L,e);\n", debugFieldName);
		method.addCode("}\n");
		
		cp.addMethod(method.build());
	}
	
	private static boolean isList(TypeName type) {
		return isList(type.toString());
	}
	private static boolean isList(String typeName) {
		return typeName.startsWith("java.util.List") || typeName.startsWith("hr.hrg.hipster.sql.ImmutableList");
	}

	public static void add_encode_mongo(TypeSpec.Builder builder, EntityDef def, ProcessingEnvironment processingEnv){
		
		addMethod(builder, PUBLIC(), void.class, "encode", method -> {		
			method.addAnnotation(Override.class);
			
			addParameter(method, BsonWriter.class, "writer");
			addParameter(method, def.type, "value");
			method.addParameter(EncoderContext.class, "encoderContext");
					
			method.addCode("if(value == null) {\n");
			method.addCode("\twriter.writeNull();\n");
			method.addCode("\treturn;\n");
			method.addCode("}\n\n");

			method.addCode("writer.writeStartDocument();\n\n");
			
			if(def.jsonTypeInfo != null) {
				method.addCode("writer.writeString($S,$S);\n","_t",def.entityName);
			}
			
			int count = def.getProps().size();
			for (int i = 0; i < count; i++) {
				Property prop = def.getProps().get(i);
				String fieldName = def.genOptions.isMongoUseFieldName() ? prop.fieldName : prop.columnName;
				
				if(prop.jsonIgnore) continue;
				
				String typeStr = prop.type.toString();
				boolean primitive = prop.type.isPrimitive();
				
				if(prop.array) {
					ArrayTypeName arrayTypeName = (ArrayTypeName)prop.type;
					addWriteArrayMongo(method, prop, arrayTypeName, def, fieldName);
				}else if(prop.parametrized && isList(prop.type)){
//					ParameterizedTypeName parameterizedTypeName = (ParameterizedTypeName)prop.type;
					method.addCode("// $L $T<",prop.fieldName, prop.parameterizedOuterRaw);
					for(TypeName arg:prop.typeArguments)
						method.addCode("$T,",arg);
					method.addCode(">\n");
					addWriteListMongo(method, prop, def, fieldName);
					
					//					typeHandlersBlock.add("$T.class",parameterizedTypeName.rawType);
//					for(TypeName ta: parameterizedTypeName.typeArguments){
//						typeHandlersBlock.add(",$T.class",ta);					
//					}
//					typeHandlersBlock.add(");\n");
				}else if(primitive || prop.type.isBoxedPrimitive()){
					TypeName unboxed = prop.type.unbox();
					if(TypeName.INT.equals(unboxed) 
							|| TypeName.SHORT.equals(unboxed)
							|| TypeName.BYTE.equals(unboxed)
							){
						addWriteMongo(method, "writeInt32", prop, !primitive, def, fieldName);
					}else if(TypeName.LONG.equals(unboxed)){
						addWriteMongo(method, "writeInt64", prop, !primitive, def, fieldName);
					}else if(TypeName.INT.equals(unboxed)){
						addWriteMongo(method, "writeInt32", prop, !primitive, def, fieldName);
					}else if(TypeName.DOUBLE.equals(unboxed) || TypeName.FLOAT.equals(unboxed)){
						addWriteMongo(method, "writeDouble", prop, !primitive, def, fieldName);
					}else if(TypeName.BOOLEAN.equals(unboxed)){
						addWriteMongo(method, "writeBoolean", prop, !primitive, def, fieldName);
					}else if(TypeName.CHAR.equals(unboxed)){
						if(!primitive){
							method.addCode("if (value.$L() == null)\n",prop.getterName);
							method.addCode("\twriter.writeNull();\n");
							method.addCode("else\n");
							method.addCode("\t");
						}
						method.addCode("writer.writeString(new char[]{value.$L()},0,1);\n",prop.getterName);
					}
				} else if(typeStr.equals("java.lang.String")){
					addWriteMongo(method, "writeString", prop, true, def, fieldName);
				} else {
					addWriteMongo(method, null, prop, true, def, fieldName);
				}
				method.addCode("\n");
			}
			
			method.addCode("writer.writeEndDocument();\n");
			
		});
	}	
	
	public static void addWriteArrayMongo(Builder method, Property prop, ArrayTypeName arrayTypeName, EntityDef def, String fieldName) {
		TypeName type = arrayTypeName.componentType;
		boolean primitive = type.isBoxedPrimitive();
		TypeName unboxed = primitive ? type.unbox(): type;
		String typeStr = prop.type.toString();
		String prefix = "";
		if(def.genOptions.isMongoSkipNull()) {
			prefix = "\t";
			method.addCode("if(value.$L() != null){\n", prop.getterName);			
		}
		method.addCode(prefix+"writer.writeName($S);\n", fieldName);
		if(TypeName.INT.equals(unboxed)
				|| TypeName.SHORT.equals(unboxed)
				|| TypeName.INT.equals(unboxed)
				|| TypeName.LONG.equals(unboxed)
				|| TypeName.DOUBLE.equals(unboxed)
				|| TypeName.FLOAT.equals(unboxed)
				|| TypeName.BOOLEAN.equals(unboxed)
				|| typeStr.equals("java.lang.String")){
			method.addCode(prefix+"$T.encodeArray(writer,value.$L());\n", MongoEncode.class, prop.getterName);			
		}else {
			method.addCode(prefix+"$T.encodeArray(($T<$T>)_codecs[$L],writer,value.$L(), encoderContext);\n", MongoEncode.class,Codec.class, type, prop.ordinal, prop.getterName);						
		}
		if(def.genOptions.isMongoSkipNull()) {
			method.addCode("}\n");			
		}

	}

	public static void addWriteListMongo(Builder method, Property prop, EntityDef def, String fieldName) {
		TypeName type = prop.componentType;
		boolean primitive = type.isBoxedPrimitive();
		TypeName unboxed = primitive ? type.unbox(): type;
		String typeStr = prop.type.toString();

		String prefix = "";
		if(def.genOptions.isMongoSkipNull()) {
			prefix = "\t";
			method.addCode("if(value.$L() != null){\n", prop.getterName);			
		}
		method.addCode(prefix+"writer.writeName($S);\n", fieldName);
		String encodeMethod = null; 
		if(TypeName.INT.equals(unboxed)) {
			encodeMethod = "encodeListInteger";
		}else if(TypeName.LONG.equals(unboxed)) {
			encodeMethod = "encodeListLong";
		}else if(TypeName.FLOAT.equals(unboxed)) {
			encodeMethod = "encodeListFloat";
		}else if(TypeName.DOUBLE.equals(unboxed)) {
			encodeMethod = "encodeListDouble";
		}else if(TypeName.SHORT.equals(unboxed)) {
			encodeMethod = "encodeListShort";
		}else if(TypeName.BOOLEAN.equals(unboxed)) {
			encodeMethod = "encodeListBoolean";
		}else if(typeStr.equals("java.lang.String")) {
			encodeMethod = "encodeListString";
		}
		
		if(encodeMethod == null) {
			method.addCode(prefix+"$T.encodeList(($T<$T>)_codecs[$L],writer,value.$L(), encoderContext);\n", MongoEncode.class,Codec.class, type, prop.ordinal, prop.getterName);						
		} else {
			method.addCode(prefix+"$T.$L(writer,value.$L(), encoderContext);\n", MongoEncode.class, encodeMethod, prop.getterName);			
		}
		if(def.genOptions.isMongoSkipNull()) {
			method.addCode("}\n");			
		}

	}
	
	public static void addWriteMongo(Builder method, String jgenMethod, Property prop, boolean nullCheck, EntityDef def, String fieldName) {
		String prefix = "\t";
		
		boolean separateName = nullCheck || jgenMethod == null; 
		
		
		if(nullCheck) {
			if(def.genOptions.isMongoSkipNull()) {
				method.addCode("if (value.$L() != null){\n",prop.getterName);				
				if(separateName) method.addCode("\twriter.writeName($S);\n", fieldName);
			}else {				
				// name written already
				if(separateName) method.addCode("writer.writeName($S);\n", fieldName);
				method.addCode("if (value.$L() == null)\n",prop.getterName);
				method.addCode("\twriter.writeNull();\n");
				method.addCode("else{\n");
			}
		} else {			
			prefix = "";
		}
		
		if(jgenMethod == null) {
			method.addCode(prefix+"(($T<$T>)_codecs[$L]).encode(writer, value.$L(),encoderContext);\n",Codec.class,prop.type,prop.ordinal, prop.getterName);
		}else {
			if(nullCheck)// name written already
				method.addCode(prefix+"writer.$L(value.$L());\n",jgenMethod,prop.getterName);
			else
				method.addCode(prefix+"writer.$L($S, value.$L());\n",jgenMethod,fieldName,prop.getterName);
		}
		
		if(nullCheck) method.addCode("}\n");
	}
	
	private void add_fromResultSet(TypeSpec.Builder cp, EntityDef def) {
		
		MethodSpec.Builder method = methodBuilder(PUBLIC().FINAL(), def.type, "fromResultSet");
		
		method.addParameter(ResultSet.class, "rs");
		method.addException(java.sql.SQLException.class);

		CodeBlock.Builder returnValue = CodeBlock.builder().add("$T out = new $T(",def.typeImmutable, def.typeImmutable);
		genPrepValueVars(def, method, returnValue, true, false);
				
		cp.addMethod(method.build());

		
		if(def.genOptions.isGenVisitor()) {
			method = methodBuilder(PUBLIC().FINAL(), "visitResult");
			
			method.addParameter(ResultSet.class, "rs");
			method.addParameter(def.typeVisitor, "visitor");
			method.addException(java.sql.SQLException.class);
			
			returnValue = CodeBlock.builder().add("visitor.visit(",def.typeImmutable);
			genPrepValueVars(def, method, returnValue, false, true);
			
			cp.addMethod(method.build());
		}

	}
	
	public void genPrepValueVars(EntityDef def, MethodSpec.Builder method, CodeBlock.Builder returnValue, boolean withReturn, boolean skipRestColumn) {
		CodeBlock.Builder block = CodeBlock.builder();
		Property primaryProp = def.getPrimaryProp();
		block.add("String __col=\"\";\n");
		int i=0;
		if(primaryProp != null) {
			genPrepValue(block, returnValue, primaryProp,i);
			i++;
		}
		
		block.add("try{\n");
		block.indent();
		for(Property p: def.getProps()) {
			if(skipRestColumn && p.isKeepRest()) continue;
			if((primaryProp == null || !p.fieldName.equals(primaryProp.fieldName)) && !p.isTransient()) {
				genPrepValue(block, returnValue, p, i);
				i++;
			}
		}

		block.add("\n");
		returnValue.add(");\n");
		if(withReturn) {			
			returnValue.add("this.init(out);\n");
			returnValue.add("return out;\n");
		}
		block.add(returnValue.build());
		block.unindent();
		Object fieldnameForCatch = primaryProp == null ? "null" : primaryProp.fieldName;
		Class<?> entityMetaClass = def.genOptions.isGenMongo() ? MongoEntityMeta.class : EntityMeta.class;
		block.add("\n} catch(Throwable e){ throw $T.errEntity($L,ENTITY_CLASS,__col,e); }\n", 
				EntityMeta.class,
				fieldnameForCatch
				);
		
		method.addCode(block.build());
	}

	public void genPrepValue(com.squareup.javapoet.CodeBlock.Builder block, CodeBlock.Builder returnValue, Property p, int i) {
		block.add("__col=$S;$T $L",p.fieldName, p.type, p.fieldName);
		
		String getter = getterName(p);
		
		if(getter == null){
			block.add(" = ($T)_typeHandler[$L].get(rs,$L);\n",p.type,p.ordinal, p.ordinal+1);
		}else{
			block.add(" = rs."+getter+"("+(p.ordinal+1)+");\n");
		}
		
		if(i>0) returnValue.add(", ");
		returnValue.add(p.fieldName);
	}
	
	private void addColumnsDef(com.squareup.javapoet.TypeSpec.Builder cp, Builder constr, EntityDef def, ClassName columnMetaBase) {
		List<String> colNames = new ArrayList<>();
		List<String> enumNames = new ArrayList<>();
		Map<String, String> colMap = new HashMap<>();
		for(Property p:def.props) {
			if(p.isTransient()) continue;
			colMap.put(p.columnName, p.fieldName);
			if(p.isTransient()) continue;
			colNames.add(p.columnName);
			enumNames.add(p.fieldName);
		}

		final StringBuffer arrStr = new StringBuffer("(");
		final StringBuffer str = new StringBuffer("");

		String delim = "";
		for (String col : colNames) {
			arrStr.append(delim); str.append(delim);
			arrStr.append("\"").append(col).append("\"");
			str.append('"').append(col).append('"');
			delim = ",";
		}
		arrStr.append(")");

		StringBuffer arr = new StringBuffer("{")  ;
		delim = "";
		for (String col : enumNames) {
			arr.append(delim);
			arr.append(col);
			delim = ",";
		}

		arr.append("}");
		
		constr.addCode("\n");
		
		int ordinal = 0;
		for(Property prop: def.getProps()){
			if(prop.isTransient()) continue;

			// type or raw type
			TypeName rawType = prop.type;
			if(prop.type instanceof ParameterizedTypeName){				
				ParameterizedTypeName parameterizedTypeName = (ParameterizedTypeName)prop.type;
				rawType = parameterizedTypeName.rawType;
			}
//			constr.addCode(codeBlock)
			com.squareup.javapoet.CodeBlock.Builder codeBlock = CodeBlock.builder();
			codeBlock.add("$L = new $T<$T>($L, $S", prop.fieldName, columnMetaBase, rawType.box(), ordinal, prop.name);
			codeBlock.add(",$S",prop.columnName);
			codeBlock.add(",$S",prop.getterName);
			codeBlock.add(",$L",prop.required ? "true":"false");
			codeBlock.add(",this");
			
			
			codeBlock.add(",$T.class",rawType.box());

			if(prop.isPrimitive())
				codeBlock.add(",$T.class",prop.type);
			else
				codeBlock.add(",null");
			
			codeBlock.add(",$S",prop.sql);
			if(def.genOptions.isGenSql())
				codeBlock.add(",_typeHandler[$L]",ordinal);
			else
				codeBlock.add(",null");

			// type parameters if any
			if(prop.type instanceof ParameterizedTypeName){				
				ParameterizedTypeName parameterizedTypeName = (ParameterizedTypeName)prop.type;
				for(TypeName ta: parameterizedTypeName.typeArguments){
					codeBlock.add(",$T.class",ta);					
				}
			}
			codeBlock.add(")");
			
			if(prop.annotationsWithDefaults.size() == 0) {
				// CASE: Reflection
				// force initialisation of "annotations" field in ColumnMeta to empty array, 
				// (we know there are none, so reflection can be skipped for this one)
				codeBlock.add(".withAnnotations()");
				

				// CASE: Generated (no reflection)
				// - comment line above 
				// - make sure "annotations" field in ColumnMeta is initialised to: new Annotation[0]
				
			}else if(prop.annotationsWithDefaults.size() > 0) {
				// CASE: Generated (no reflection)
				// - uncomment all lines in this block
				if(def.genOptions.genAnnotations == BooleanEnum.TRUE) {					
					codeBlock.add(".withAnnotations(");
					int i=0;
					codeBlock.indent();
					codeBlock.indent();
					for(AnnotationSpec spec: prop.annotationsWithDefaults) {
						if(i>0) codeBlock.add(", ");
						
						codeBlock.add("\nannotation($T.class", spec.type);
						int j=0;
						for(Entry<String, List<CodeBlock>> elem:spec.members.entrySet()) {
							codeBlock.add(", ");
							
							codeBlock.add("$S,",elem.getKey());
							if(elem.getValue().size() == 1) {
								codeBlock.add(elem.getValue().get(0));							
							}else {
								codeBlock.add("new Object[]{");
								for(int k=0; k<elem.getValue().size(); k++) {
									if(k>0) codeBlock.add(", ");
									codeBlock.add(elem.getValue().get(k));
								}
								codeBlock.add("}");
							}
							j++;
						}
						codeBlock.add(")");
						i++;
					}
					codeBlock.add(")");				
					codeBlock.unindent();
					codeBlock.unindent();
				}

			}
			codeBlock.add(";\n");
			constr.addCode(codeBlock.build());
			ordinal++;
		}
		
		
//		addField(constr,PRIVATE().STATIC().FINAL(), String.class, "COLUMNS_STR", 
//				field->field.initializer("$S",str.toString()));
//		
		addField(cp,PUBLIC().STATIC().FINAL(), int.class, "COLUMN_COUNT", 
				field->field.initializer(""+def.coulmnCount));
//		
//		addField(constr,PRIVATE().STATIC().FINAL(), parametrized(ImmutableList.class, String.class), "COLUMN_NAMES", 
//				field->field.initializer("ImmutableList.safe"+arrStr.toString()));
//		
//		addField(constr,PRIVATE().STATIC().FINAL(), ArrayTypeName.of(columnMetaBase), "COLUMN_ARRAY", 
//				field->field.initializer(arr.toString()));
//
//		addField(cp,PUBLIC().FINAL(), parametrized(ImmutableList.class, columnMetaBase), "_columns");

		constr.addCode("\n");
		constr.addCode("_columnArray = new $T[]$L;\n",columnMetaBase, arr.toString());
		constr.addCode("_columns =  $T.safe(($T[])_columnArray);\n",ImmutableList.class,columnMetaBase);
		
		Collections.sort(enumNames);
		Collections.sort(colNames);

		arr.setLength(0);
		str.setLength(0);
		arr.append("{");
		str.append("{");

		delim = "";
		for (String col : colNames) {
			arr.append(delim); str.append(delim);
			arr.append(colMap.get(col));
			str.append('"').append(col).append('"');
			delim = ",";
		}
		arr.append("}");
		str.append("}");

		constr.addCode("_columnArraySorted = new $T[]$L;\n",columnMetaBase, arr);
		constr.addCode("_columnArraySortedStr = COLUMN_ARRAY_SORTED_STR;\n");
		constr.addCode("this._columnCount = COLUMN_COUNT;\n");

//		addField(constr,PRIVATE().STATIC().FINAL(), ArrayTypeName.of(columnMetaBase), "COLUMN_ARRAY_SORTED", 
//				field->field.initializer(arr.toString()));
//		
		addField(cp,PRIVATE().STATIC().FINAL(), ArrayTypeName.of(String.class), "COLUMN_ARRAY_SORTED_STR", 
				field->field.initializer(str.toString()));

		
		arr = new StringBuffer();
		str.setLength(0);
		arr.append("{");
		str.append("{");

		delim = "";
		for (String col : enumNames) {
			arr.append(delim); str.append(delim);
			arr.append(col);
			str.append('"').append(col).append('"');
			delim = ",";
		}
		arr.append("}");
		str.append("}");
		
		constr.addCode("_fieldArraySorted = new $T[]$L;\n",columnMetaBase, arr);
		constr.addCode("_fieldArraySortedStr = FIELD_ARRAY_SORTED_STR;\n");
		addField(cp,PRIVATE().STATIC().FINAL(), ArrayTypeName.of(String.class), "FIELD_ARRAY_SORTED_STR", 
				field->field.initializer(str.toString()));

	}	
}
