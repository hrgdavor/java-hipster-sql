package hr.hrg.hipster.processor;

import static java.lang.Character.*;

import java.lang.annotation.*;
import java.util.*;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import javax.persistence.*;
import javax.tools.Diagnostic.*;

import com.squareup.javapoet.*;
import com.squareup.javapoet.AnnotationSpec.*;

import hr.hrg.hipster.entity.*;
import hr.hrg.hipster.sql.*;
import hr.hrg.hipster.type.*;
import hr.hrg.javapoet.PoetUtil;

class Property {
	/** final, and can only be set in constructor */
	public boolean readOnly;
	public String name;
	public String fieldName;
	public String getterName;
	public boolean required;
	public boolean keepRest;
	public String setterName;
	public String columnName;
	public String initial;
	public String tableName = "";
	public String sql = "";
	public TypeName type;
	public TypeName customType;
	public String customTypeKey = "";
	public ExecutableElement method;
	public List<AnnotationSpec> annotations = new ArrayList<>();
	public List<AnnotationSpec> annotationsWithDefaults = new ArrayList<>();
	public boolean jsonIgnore;
	public boolean array;
	public boolean collection;
	public int ordinal;
	public TypeMirror typeMirror;
	public TypeName componentType;
	public List<TypeName> typeArguments;
	public boolean parametrized;
	public boolean isTransient;
	public ClassName parameterizedOuterRaw;
	
	public Property(String getter, TypeName type, TypeMirror typeMirror, ExecutableElement method, String tableName, boolean defaultColumnsRequired, ProcessingEnvironment processingEnv, int ordinal){
		this.getterName = getter;
		this.type = type;
		this.typeMirror = typeMirror;
		this.tableName = tableName;
		this.ordinal = ordinal;
		this.required = defaultColumnsRequired;
		String name = null;

		List<? extends AnnotationMirror> list = method.getAnnotationMirrors();
		if(list.size() >0){				
			for(AnnotationMirror mirror: list){
				if("com.fasterxml.jackson.annotation.JsonIgnore".equals(mirror.getAnnotationType().toString())){
					this.jsonIgnore = true;
				}
			}			
		}
		
		this.method = method;
		
		if(getter.startsWith("get") || getter.startsWith("has")) {
			name = getter.substring(3);
		}else
			name = getter.substring(2);
		setterName = "set"+name;

		this.name = name = Character.toLowerCase(name.charAt(0))+name.substring(1);
		if(PoetUtil.isJavaKeyword(name)) this.name = "_"+name;
		this.fieldName = this.name;
		
		this.columnName = this.name;
		Column columnAnnotation = method.getAnnotation(Column.class);
		if(columnAnnotation != null){
			if(!columnAnnotation.name().isEmpty()) this.columnName = columnAnnotation.name();
			if(!columnAnnotation.table().isEmpty()) tableName = columnAnnotation.table();
		}
		
		Transient transient1 = method.getAnnotation(Transient.class);
		java.beans.Transient transient2 = method.getAnnotation(java.beans.Transient.class);
		isTransient = transient1 != null || transient2 != null;
		
		HipsterColumn hipsterColumn = method.getAnnotation(HipsterColumn.class);
		if(hipsterColumn != null){
			if(hipsterColumn.required() != BooleanEnum.DEFAULT) required = hipsterColumn.required() == BooleanEnum.TRUE; 
			if(hipsterColumn.keepRest()) { 
				keepRest = true;
			}
			if(!hipsterColumn.name().isEmpty()) this.columnName = hipsterColumn.name();
			if(hipsterColumn.initial().length >0) this.initial = hipsterColumn.initial()[0];
			this.sql = hipsterColumn.sql();
			if(!hipsterColumn.table().isEmpty()) this.tableName = hipsterColumn.table();
			try {
				if(hipsterColumn.customType().getTypeName().equals(ICustomType.class.getTypeName())) {
					this.customType = ClassName.get(hipsterColumn.customType());// will likely always throw error					
				}
			}catch (MirroredTypeException e) {
				if(!"hr.hrg.hipster.sql.ICustomType".equals(e.getTypeMirror().toString())) {					
					this.customType = ClassName.get(e.getTypeMirror());
				}
			}
			if(!hipsterColumn.customTypeKey().isEmpty()) this.customTypeKey = hipsterColumn.customTypeKey();
		}

		List<? extends AnnotationMirror> annotationMirrors = method.getAnnotationMirrors();
		for (AnnotationMirror mirror : annotationMirrors) {
			AnnotationSpec annotationSpec = getAnnotation(mirror, processingEnv);
			if(!annotationSpec.type.toString().startsWith("hr.hrg.hipster.sql")) {
				annotationsWithDefaults.add(annotationSpec);
				annotations.add(AnnotationSpec.get(mirror));
			}
		}
				
		if(type instanceof ParameterizedTypeName) {
			ParameterizedTypeName parameterizedTypeName = (ParameterizedTypeName) type;
			this.parametrized = true;
			parameterizedOuterRaw = parameterizedTypeName.rawType;
			typeArguments = parameterizedTypeName.typeArguments;
			componentType = typeArguments.get(0);
		}
		
		if(type instanceof ArrayTypeName) {
			ArrayTypeName arrayTypeName = (ArrayTypeName) type;
			this.array = true;
			componentType = arrayTypeName.componentType;
		}
		if(keepRest && isTransient) {
			throw new RuntimeException("Field "+fieldName+" can not be both transient and marked for keepRest");
		}
	}

	public boolean isPrimitive(){
		return type.isPrimitive();
	}
	
	static <T> T checkNotNull(T reference, String format, Object... args) {
		if (reference == null)
			throw new NullPointerException(String.format(format, args));
		return reference;
	}

	static String characterLiteralWithoutSingleQuotes(char c) {
		// see https://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.6
		switch (c) {
		case '\b':
			return "\\b"; /* \u0008: backspace (BS) */
		case '\t':
			return "\\t"; /* \u0009: horizontal tab (HT) */
		case '\n':
			return "\\n"; /* \u000a: linefeed (LF) */
		case '\f':
			return "\\f"; /* \u000c: form feed (FF) */
		case '\r':
			return "\\r"; /* \u000d: carriage return (CR) */
		case '\"':
			return "\""; /* \u0022: double quote (") */
		case '\'':
			return "\\'"; /* \u0027: single quote (') */
		case '\\':
			return "\\\\"; /* \u005c: backslash (\) */
		default:
			return isISOControl(c) ? String.format("\\u%04x", (int) c) : Character.toString(c);
		}
	}

	public static AnnotationSpec getAnnotation(AnnotationMirror annotation, ProcessingEnvironment processingEnv) {
		TypeElement element = (TypeElement) annotation.getAnnotationType().asElement();
		AnnotationSpec.Builder builder = AnnotationSpec.builder(ClassName.get(element));
		Elements elements = processingEnv.getElementUtils();
		Visitor visitor = new Visitor(builder, processingEnv);
		
		Map<? extends ExecutableElement, ? extends AnnotationValue> map = elements.getElementValuesWithDefaults(annotation);
		for (ExecutableElement executableElement : map.keySet()) {
			String name = executableElement.getSimpleName().toString();
			AnnotationValue value = map.get(executableElement);
			if(value != null) {
				value.accept(visitor, name);
			}else {
				System.out.println("NULL VALUE "+annotation.getAnnotationType()+" "+name);
			}
		}
		return builder.build();
	}

	private static class Visitor extends SimpleAnnotationValueVisitor7<Builder, String> {
		final Builder builder;
		private ProcessingEnvironment processingEnv;

		Visitor(Builder builder, ProcessingEnvironment processingEnv) {
			super(builder);
			this.builder = builder;
			this.processingEnv = processingEnv;
		}

		@Override
		protected Builder defaultAction(Object o, String name) {
			return addMemberForValue(builder,name, o);
		}

		@Override
		public Builder visitAnnotation(AnnotationMirror a, String name) {
			return builder.addMember(name, "$L", getAnnotation(a, processingEnv));
		}

		@Override
		public Builder visitEnumConstant(VariableElement c, String name) {
			return builder.addMember(name, "$T.$L", c.asType(), c.getSimpleName());
		}

		@Override
		public Builder visitType(TypeMirror t, String name) {
			return builder.addMember(name, "$T.class", t);
		}

		@Override
		public Builder visitArray(List<? extends AnnotationValue> values, String name) {
			for (AnnotationValue value : values) {
				value.accept(this, name);
			}
			return builder;
		}
	}
	
	public boolean isTransient() {
		return isTransient;
	}
	
	public boolean isKeepRest() {
		return keepRest;
	}
	
	static Builder addMemberForValue(Builder builder, String memberName, Object value) {
	      checkNotNull(memberName, "memberName == null");
	      checkNotNull(value, "value == null, constant non-null value expected for %s", memberName);
	      if (value instanceof Class<?>) {
	        return builder.addMember(memberName, "$T.class", value);
	      }
	      if (value instanceof Enum) {
	        return builder.addMember(memberName, "$T.$L", value.getClass(), ((Enum<?>) value).name());
	      }
	      if (value instanceof String) {
	        return builder.addMember(memberName, "$S", value);
	      }
	      if (value instanceof Float) {
	        return builder.addMember(memberName, "$Lf", value);
	      }
	      if (value instanceof Character) {
	        return builder.addMember(memberName, "'$L'", characterLiteralWithoutSingleQuotes((char) value));
	      }
	      return builder.addMember(memberName, "$L", value);
	    }
	

}