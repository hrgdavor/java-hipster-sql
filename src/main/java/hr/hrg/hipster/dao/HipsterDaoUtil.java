package hr.hrg.hipster.dao;

public class HipsterDaoUtil {

	public static final String propToGetter(String prop, String type){
		if("boolean".equals(type)) 
			return "is"+Character.toUpperCase(prop.charAt(0))+prop.substring(1);
		
		return "get"+Character.toUpperCase(prop.charAt(0))+prop.substring(1);
	}

	public static final String propToSetter(String prop){
		return "set"+Character.toUpperCase(prop.charAt(0))+prop.substring(1);
	}
	
}
