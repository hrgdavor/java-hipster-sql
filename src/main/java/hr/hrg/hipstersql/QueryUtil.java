package hr.hrg.hipstersql;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryUtil {

	/** Short method name to build query inline */
	public static Query q(Object ... q) {
		return new Query(q);
	}

	@SafeVarargs
	public static <T> List<T> toList(T ... arr){
		return Arrays.asList(arr);
	}

	@SafeVarargs
	public static <T> T[] toArray(T ... arr){
		return arr;
	}

	public static Map<Object,Object> toMap(Object ... arr) {
		HashMap<Object, Object> map = new HashMap<>();
		for(int i=1; i<arr.length; i++){
			map.put(arr[i-1], arr[i]);
		}
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public static Query qIn(Object ...values){
		
		if(values.length == 0 && values[0] instanceof List)
			return Query.implodeValues(" IN(", (List<Object>) values[0], ",", ") ");
		
		return Query.implodeValues(" IN(", toList(values), ",", ") ");
	}

}
