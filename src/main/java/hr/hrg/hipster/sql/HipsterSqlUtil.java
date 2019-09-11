package hr.hrg.hipster.sql;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

import javax.lang.model.element.*;

import hr.hrg.hipster.dao.*;


public class HipsterSqlUtil {

	private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS = new HashMap<>();
	static {

		PRIMITIVES_TO_WRAPPERS.put(boolean.class, Boolean.class);
		PRIMITIVES_TO_WRAPPERS.put(byte.class, Byte.class);
		PRIMITIVES_TO_WRAPPERS.put(char.class, Character.class);
		PRIMITIVES_TO_WRAPPERS.put(double.class, Double.class);
		PRIMITIVES_TO_WRAPPERS.put(float.class, Float.class);
		PRIMITIVES_TO_WRAPPERS.put(int.class, Integer.class);
		PRIMITIVES_TO_WRAPPERS.put(long.class, Long.class);
		PRIMITIVES_TO_WRAPPERS.put(short.class, Short.class);
		PRIMITIVES_TO_WRAPPERS.put(void.class, Void.class);
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<T> wrap(Class<T> c) {
		return c.isPrimitive() ? (Class<T>) PRIMITIVES_TO_WRAPPERS.get(c) : c;
	}
	  
	private static boolean yodaPresent = false;
	private static boolean jacksonPresent = false;
	private static boolean persistenceApiPresent = false;
	
	// might be going too far with allowing to skip even slf4j, but hey, it is just for those that really want to
	private static boolean slf4jApiPresent = false;

	static {
		try {
			Class.forName("org.joda.time.DateTime");
			yodaPresent = true;
		} catch (ClassNotFoundException e) {
			// Joda DateTime classes not present, yoda DateTime support will be skipped
		}
	}
	
	static {
		try {
			Class.forName("javax.persistence.Column");
			persistenceApiPresent = true;
		} catch (ClassNotFoundException e) {
			// javax.persistence classes not present, javax.persistence support will be disabled
		}
	}
	
	static {
		try {
			Class.forName("org.slf4j.LoggerFactory");
			slf4jApiPresent = true;
		} catch (ClassNotFoundException e) {
			// org.slf4j classes not present, org.slf4j support will be disabled
		}
	}

	static {
		try {
			Class.forName("com.fasterxml.jackson.annotation.LoggerFactory.JsonIgnore");
			jacksonPresent = true;
		} catch (ClassNotFoundException e) {
			// org.slf4j classes not present, org.slf4j support will be disabled
		}
	}

	public static String[] entityNamesPrefixArray(TypeElement clazz){
		String[] ret = new String[2];
		
		Element enclosingElement = clazz.getEnclosingElement();
		String qName = clazz.getQualifiedName().toString();

		
		int idx = qName.lastIndexOf('.');
		ret[1] = qName.substring(idx+1);

		if(enclosingElement instanceof PackageElement){
			ret[0] = qName.substring(0, idx);
		}else{
			String packageName = qName.substring(0, idx)+"_";
			ret[0] = packageName; 
			
		}
		
		return ret;
	}

	public static <C extends BaseColumnMeta> Query.ImmutableQuery selectQueryForEntity(IEntityMeta<?, ?, C> meta) {
		int columnCount = meta.getColumnCount()*2-1;
		List<C> columns = meta.getColumns();
		Object[] tmp = new Object[columnCount+4];
		
		// SELECT
		tmp[0] = "SELECT ";
		// all columns
		int column = 0;
		for(int i=1; i<=columnCount; i++) {
			if(i % 2 == 0) 
				tmp[i]=",";
			else 
				tmp[i] = columns.get(column++);
		}
		// from
		tmp[columnCount+1] = " FROM ";
		// table
		tmp[columnCount+2] = meta.getTable();
		tmp[columnCount+3] = " ";

		return new Query.ImmutableQuery(ImmutableList.safe(tmp));
	}
	
	public static String entityNamesPrefix(Class<?> clazz){
		return clazz.getName().replaceAll("\\$", "_.");
	}

	public static String[] entityNamesPrefixArray(Class<?> clazz){
		String[] ret = new String[2];
		
		String qName = entityNamesPrefix(clazz);
		
		int idx = qName.lastIndexOf('.');
		ret[0] = qName.substring(0, idx);
		ret[1] = qName.substring(idx+1);
		
		return ret;
	}
	
	public static boolean isYodaPresent(){
		return yodaPresent;
	}
	
	public static boolean isPersistenceApiPresent() {
		return persistenceApiPresent;
	}
	
	public static boolean isSlf4jApiPresent() {
		return slf4jApiPresent;
	}
	
	public static boolean isJacksonPresent() {
		return jacksonPresent;
	}
	
	public static String join(String delim, Object ...objects){
		if(objects.length == 0) return "";
		StringBuilder b = new StringBuilder(objects[0].toString());
		for(int i =1; i<objects.length; i++) b.append(delim).append(objects[i]);
		return b.toString();
	}

	public static String joinClassNames(String delim, Class<?> ...classes){
		if(classes.length == 0) return "";
		StringBuilder b = new StringBuilder(classes[0].getName());
		for(int i =1; i<classes.length; i++) b.append(delim).append(classes[i].getName());
		return b.toString();
	}
	
	public static final String propToGetter(String prop, String type){
		if("boolean".equals(type)) 
			return "is"+Character.toUpperCase(prop.charAt(0))+prop.substring(1);
		
		return "get"+Character.toUpperCase(prop.charAt(0))+prop.substring(1);
	}

	public static final String propToSetter(String prop){
		return "set"+Character.toUpperCase(prop.charAt(0))+prop.substring(1);
	}
	
    /**
     * modified from https://github.com/leangen/geantyref/blob/master/src/main/java/io/leangen/geantyref/TypeFactory.java
     * Creates an instance of an annotation.
     *
     * @param annotationType The {@link Class} representing the type of the annotation to be created.
     * @param values A map of values to be assigned to the annotation elements.
     * @param <A> The type of the annotation.
     * @return An {@link Annotation} instanceof matching {@code annotationType}
     */
    @SuppressWarnings("unchecked")
    public static <A extends Annotation> A annotation(Class<A> annotationType, Map<String, Object> values) {
    	if(values == null) values = Collections.emptyMap();
        return (A) Proxy.newProxyInstance(annotationType.getClassLoader(),
                new Class[] { annotationType },
                new AnnotationInvocationHandler(annotationType,  values));
    }
    
    @SuppressWarnings("unchecked")
	public static <A extends Annotation> A annotation(Class<A> annotationType, Object ... values) {
    	HashMap<String, Object> map = new HashMap<>();
    	for(int i=1; i<values.length; i+=2) {
    		map.put((String) values[i-1], values[i-1]);
    	}
        return (A) Proxy.newProxyInstance(annotationType.getClassLoader(),
                new Class[] { annotationType },
                new AnnotationInvocationHandler(annotationType,  map));
    }
}
