package hr.hrg.hipster.sql;

import javax.lang.model.element.*;

public class HipsterSqlUtil {
		
	private static boolean yodaPresent = false;
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
}
