package hr.hrg.hipster.processor;

import static hr.hrg.hipster.processor.HipsterProcessorUtil.*;
import static hr.hrg.javapoet.PoetUtil.*;

import java.sql.*;
import java.util.*;

import com.squareup.javapoet.*;
import com.squareup.javapoet.MethodSpec.*;

import hr.hrg.hipster.change.*;
import hr.hrg.hipster.dao.*;
import hr.hrg.hipster.sql.*;
import hr.hrg.hipster.type.*;


public class GenMeta {

	public TypeSpec.Builder gen(EntityDef def, ClassName columnMetaBase) {
		
		TypeSpec.Builder cp = classBuilder(PUBLIC(), def.typeMeta);

		addComments(cp, def, columnMetaBase);
		
		Property primaryProp = def.getPrimaryProp();
		TypeName primaryType = primaryProp == null ? TypeName.get(Object.class) : primaryProp.type;
	
		cp.superclass(parametrized(EntityMeta.class,def.type, primaryType, columnMetaBase, def.typeVisitor));
		
		// public static final Class<SampleEntity> ENTITY_CLASS = SampleEntity.class;
		addField(cp, PRIVATE().STATIC().FINAL(), parametrized(Class.class, def.type), "ENTITY_CLASS", "$T.class",def.type);	
		
		int ordinal = 0;
		for(Property prop: def.getProps()){
			// type or raw type
			TypeName rawType = prop.type;
			if(prop.type instanceof ParameterizedTypeName){				
				ParameterizedTypeName parameterizedTypeName = (ParameterizedTypeName)prop.type;
				rawType = parameterizedTypeName.rawType;
			}

			addField(cp, PUBLIC().FINAL(), parametrized(columnMetaBase, rawType.box()), prop.fieldName);	
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
		constr.addCode("super(_ordinal, $S, ENTITY_CLASS);\n", def.tableName);
		addSetterParameter(constr, ordinalField, null);
		int i=0;
		
		
		constr.addCode("_typeHandler = new $T<?>[COLUMN_COUNT];\n", ICustomType.class);
		
		CodeBlock.Builder typeHandlersBlock = CodeBlock.builder();
		typeHandlersBlock.add("if(hipster != null){\n");
		typeHandlersBlock.indent();
		typeHandlersBlock.add("$T _typeSource = hipster.getTypeSource();\n", TypeSource.class);
		boolean hasGetters = false;
		
		for(Property p:def.getProps()) {
			ParameterizedTypeName parametrizedCustomType = parametrized(ICustomType.class, p.type.box());
			if(p.customType != null) {
				typeHandlersBlock.add("_typeHandler["+i+"] = ($T) _typeSource.getInstanceRequired($T.class);\n", parametrizedCustomType, p.customType);				
			}else if(!p.customTypeKey.isEmpty()){
				typeHandlersBlock.add("_typeHandler["+i+"] = ($T) _typeSource.getNamedRequired($S);\n", parametrizedCustomType, p.customTypeKey);
			}else {				
				typeHandlersBlock.add("_typeHandler["+i+"] = ($T) _typeSource.getForRequired(", parametrizedCustomType);
				hasGetters = true;
				if(p.type instanceof ParameterizedTypeName){
					ParameterizedTypeName parameterizedTypeName = (ParameterizedTypeName)p.type;
					typeHandlersBlock.add("$T.class",parameterizedTypeName.rawType);
					for(TypeName ta: parameterizedTypeName.typeArguments){
						typeHandlersBlock.add(",$T.class",ta);					
					}
					typeHandlersBlock.add(");\n");
				}else{
					typeHandlersBlock.add("$T.class);\n", p.type.box());
				}
			}
			
			i++;
		}
		
		typeHandlersBlock.unindent();
		typeHandlersBlock.add("}\n");
		
		constr.addCode(typeHandlersBlock.build());

		addColumnsDef(cp, constr, def, columnMetaBase);
		
//		addconstructor(cp, PUBLIC(), method-> {
//			method.addParameter(HipsterSql.class, "hipster");
//			method.addCode("this(hipster.getGetterSource());\n");
//			method.addCode("hipster.getReaderSource().registerFor(this, $T.class);\n",def.type);
//		});
		
		cp.addMethod(constr.build());
		
		add_fromResultSet(cp, def);

//		TypeName returnType = parametrized(EnumArrayUpdateDelta.class,columnMetaBase);
//		addMethod(cp,PRIVATE().STATIC().FINAL(), returnType, "delta", delta->{
//			delta.addParameter(long.class, "changeSet");
//			delta.addParameter(ArrayTypeName.of(Object.class), "values");
//			delta.addCode("return new $T(changeSet, values, $T.COLUMN_ARRAY);\n", returnType,columnMetaBase);			
//		});
		
		if(def.genUpdate){			
			//@Override
			//public final SampleUpdate mutableCopy(Object v){ return new SmapleUpdate((Sample)v); }
			addMethod(cp,PUBLIC().FINAL(), def.typeUpdate, "mutableCopy", method->{
				method.addAnnotation(Override.class);
				addParameter(method, Object.class, "v");
				method.addCode("return new $T(($T)v);\n", def.typeUpdate, def.type);
			});
		}else{
			//@Override
			//public final IUpdatable<ColumnMeta> mutableCopy(Sample v){ throw new RuntimeExcep(v); }
			addMethod(cp,PUBLIC().FINAL(), IUpdatable.class, "mutableCopy", method->{
				method.addAnnotation(Override.class);
				addParameter(method, Object.class, "v");
				method.addCode("throw new $T($S);\n", RuntimeException.class,"can not be implemented without updater");
			});
			
		}

		//@Override
		//public final Class<Sample> getEntityClass(){ return ENTITY_CLASS; }
//		addMethod(cp,PUBLIC().FINAL(), parametrized(Class.class, def.type), "getEntityClass", method->{
//			method.addAnnotation(Override.class);
//			method.addCode("return ENTITY_CLASS;\n");
//		});
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
		
		return cp;
	}
	
	private void addComments(com.squareup.javapoet.TypeSpec.Builder cp, EntityDef def, ClassName columnMetaBase) {
		String typeDao = def.type.simpleName()+"Dao";
		cp.addJavadoc("Example dao class with proper generic arguments:\n");
		cp.addJavadoc("<pre>\n");
		cp.addJavadoc("import hr.hrg.hipster.dao.*;\n");
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
		cp.addJavadoc("import hr.hrg.hipster.dao.*;\n");
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
		
		cp.addJavadoc("Example visitor lambda:\n");
		cp.addJavadoc("<pre>\n");
		cp.addJavadoc("meta.visitResults(hc, query, (");
		int i=1;
		for(Property p:def.getProps()) {
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

	private String getterName(Property p){
		if(isType(p, "int","java.lang.Integer")){
			return "getInt";
		
		}else if(isType(p, "boolean","java.lang.Boolean")){
			return "getBoolean";
		
		}else if(isType(p, "long","java.lang.Long")){
			return "getLong";
			
		}else if(isType(p, "double","java.lang.Double")){
			return "getDouble";
			
		}else if(isType(p, "float","java.lang.Float")){
			return "getFloat";

		}else if(isType(p, "java.lang.String")){
			return "getString";
		}

		return null;
	}
	
	private void add_fromResultSet(TypeSpec.Builder cp, EntityDef def) {
		MethodSpec.Builder method = methodBuilder(PUBLIC().FINAL(), def.type, "fromResultSet");
		
		method.addParameter(ResultSet.class, "rs");
		method.addException(java.sql.SQLException.class);

		CodeBlock.Builder returnValue = CodeBlock.builder().add("return new $T(",def.typeImmutable);
		genPrepValueVars(def, method, returnValue);
		method.addCode("\n");
		returnValue.add(");");
		
		method.addCode(returnValue.build());
		
		cp.addMethod(method.build());

	
		method = methodBuilder(PUBLIC().FINAL(), "visitResult");
		
		method.addParameter(ResultSet.class, "rs");
		method.addParameter(def.typeVisitor, "visitor");
		method.addException(java.sql.SQLException.class);

		returnValue = CodeBlock.builder().add("visitor.visit(",def.typeImmutable);
		genPrepValueVars(def, method, returnValue);
		method.addCode("\n");
		returnValue.add(");");
		
		method.addCode(returnValue.build());
		
		cp.addMethod(method.build());

	}

	public void genPrepValueVars(EntityDef def, MethodSpec.Builder method, CodeBlock.Builder returnValue) {
		int i=1;
		for(Property p:def.getProps()) {
			method.addCode("$T $L",p.type, p.fieldName);
			
			String getter = getterName(p);
			
			if(getter == null){
				method.addCode(" = ($T)_typeHandler[$L].get(rs,$L);\n",p.type,i-1, i);
			}else{
				method.addCode(" = rs."+getter+"("+i+");\n");
			}
			
			// add to constructor
			if(i>1) returnValue.add(", ");
			returnValue.add(p.fieldName);			

			i++;
		}
	}
	
	private void addColumnsDef(com.squareup.javapoet.TypeSpec.Builder cp, Builder constr, EntityDef def, ClassName columnMetaBase) {
		List<String> colNames = new ArrayList<>();
		List<String> enumNames = new ArrayList<>();
		for(Property p:def.props) {
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

		final StringBuffer arr = new StringBuffer("{")  ;
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
			codeBlock.add(",this");
			
			
			codeBlock.add(",$T.class",rawType.box());

			if(prop.isPrimitive())
				codeBlock.add(",$T.class",prop.type);
			else
				codeBlock.add(",null");
			
			codeBlock.add(",$S",prop.sql);
			codeBlock.add(",_typeHandler[$L]",ordinal);

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
				// (we know there ar non, so reflection can be skipped for this one)
				codeBlock.add(".withAnnotations()");
				

				// CASE: Generated (no reflection)
				// - comment line above 
				// - make sure "annotations" field in ColumnMeta is initialised to: new Annotation[0]
				
			}else if(prop.annotationsWithDefaults.size() > 0) {
				// CASE: Generated (no reflection)
				// - uncomment all lines in this block

//				codeBlock.add(".withAnnotations(");
//				int i=0;
//				codeBlock.indent();
//				codeBlock.indent();
//				for(AnnotationSpec spec: prop.annotationsWithDefaults) {
//					if(i>0) codeBlock.add(", ");
//					
//					codeBlock.add("\nannotation($T.class", spec.type);
//					int j=0;
//					for(Entry<String, List<CodeBlock>> elem:spec.members.entrySet()) {
//						codeBlock.add(", ");
//						
//						codeBlock.add("$S,",elem.getKey());
//						if(elem.getValue().size() == 1) {
//							codeBlock.add(elem.getValue().get(0));							
//						}else {
//							codeBlock.add("new Object[]{");
//							for(int k=0; k<elem.getValue().size(); k++) {
//								if(k>0) codeBlock.add(", ");
//								codeBlock.add(elem.getValue().get(k));
//							}
//							codeBlock.add("}");
//						}
//						j++;
//					}
//					codeBlock.add(")");
//					i++;
//				}
//				codeBlock.add(")");				
//				codeBlock.unindent();
//				codeBlock.unindent();

			}
			codeBlock.add(";\n");
			constr.addCode(codeBlock.build());
			ordinal++;
		}
		
		
//		addField(constr,PRIVATE().STATIC().FINAL(), String.class, "COLUMNS_STR", 
//				field->field.initializer("$S",str.toString()));
//		
		addField(cp,PUBLIC().STATIC().FINAL(), int.class, "COLUMN_COUNT", 
				field->field.initializer(""+def.props.size()));
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

		arr.setLength(0);
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

		constr.addCode("_columnArraySorted = new $T[]$L;\n",columnMetaBase, arr);
		constr.addCode("_columnArraySortedStr = COLUMN_ARRAY_SORTED_STR;\n");
		constr.addCode("this._columnCount = COLUMN_COUNT;\n");

//		addField(constr,PRIVATE().STATIC().FINAL(), ArrayTypeName.of(columnMetaBase), "COLUMN_ARRAY_SORTED", 
//				field->field.initializer(arr.toString()));
//		
		addField(cp,PRIVATE().STATIC().FINAL(), ArrayTypeName.of(String.class), "COLUMN_ARRAY_SORTED_STR", 
				field->field.initializer(str.toString()));

	}	
}
