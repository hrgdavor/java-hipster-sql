package hr.hrg.hipstersql;

import java.util.ArrayList;
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
			return implodeValues(" IN(", (List<Object>) values[0], ",", ") ");
		
		return implodeValues(" IN(", toList(values), ",", ") ");
	}

	public static final Query implode(List<Query> queries, String glue){		
		return implode(null,queries, glue, null);
	}
	
	public static final Query implode(String prefix, List<Query> queries, String glue){
		return implode(prefix,queries, glue, null);
	}
	
	public static final Query implode(String prefix, List<Query> queries, String glue, String suffix){
		Query query = new Query();

		// remove empty queries
		List<Query> tmp = new ArrayList<Query>();
		for(Query qi:queries) if(!qi.isEmpty()) tmp.add(qi);
		queries = tmp;
		
		if(queries.size() == 0) return query;
		
		if(prefix != null) query.append(prefix);
		
		int count = queries.size();
		for(int i=0; i<count; i++) {
			if(i>0) query.append(glue);
			query.append(queries.get(i));
		}

		if(suffix != null) query.append(suffix);
		return query;
	}

	public static final Query implodeValues(List<? extends Object> queries, String glue){		
		return implodeValues(null,queries, glue, null);
	}
	
	public static final Query implodeValues(String prefix, List<? extends Object> queries, String glue){
		return implodeValues(prefix,queries, glue, null);
	}
	
	public static final Query implodeValues(String prefix, List<? extends Object> values, String glue, String suffix){
		Query query = new Query();
		
		if(values.size() == 0) return query;
		
		if(prefix != null) query.append(prefix);
		else query.append("");

		int count = values.size();
		for(int i=0; i<count; i++) {
			if(i>0) query.append(glue);
			query.appendValue(values.get(i));
		}

		if(suffix != null) query.append(suffix);
		return query;
	}	
}
