package hr.hrg.hipster.sql;

import org.slf4j.*;

public class HipsterSqlUtil {
	
	static Logger log = LoggerFactory.getLogger(HipsterSqlUtil.class);
	
	private static boolean yodaPresent = false;

	static {
		try {
			Class.forName("org.joda.time.DateTime");
			yodaPresent = true;
		} catch (ClassNotFoundException e) {
			log.warn("Joda DateTime classes not present, yoda DateTime support will be skipped");
		}
	}
	
	public static boolean isYodaPresent(){
		return yodaPresent;
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
