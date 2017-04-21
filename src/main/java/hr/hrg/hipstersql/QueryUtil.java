package hr.hrg.hipstersql;

import java.util.*;

public class QueryUtil {
	
	@SafeVarargs
	/** Convert varargs to List
	 * 
	 * @param arr elements
	 * @return List
	 */
	public static final <T> List<T> toList(T ... arr){
		return Arrays.asList(arr);
	}

	@SafeVarargs
	/**Convert varargs to List<Object>
	 * 
	 * @param arr elements
	 * @return List
	 */
	public static final List<Object> toObjectList(Object ... arr){
		return Arrays.asList(arr);
	}
	
	@SafeVarargs
	/** Shortcut for new T[]{...}
	 * 
	 * @param arr elements
	 * @return array
	 */
	public static final <T> T[] toArray(T ... arr){
		return arr;
	}

	@SafeVarargs
	/** Shortcut for new Object[]{...}
	 * 
	 * @param arr elements
	 * @return Object[]
	 */
	public static final Object[] toObjectArray(Object ... arr){
		return arr;
	}

	/** Convert varargs to Map &lt;Object,Object&gt;
	 * 
	 * @param arr key value pairs
	 * @return Map key:value
	 */
	public static final Map<Object,Object> toMap(Object ... arr) {
		HashMap<Object, Object> map = new HashMap<>();
		for(int i=1; i<arr.length; i+=2){
			map.put(arr[i-1], arr[i]);
		}
		return map;
	}

	/** Convert varargs to Map &lt;Object,Object&gt;
	 * 
	 * @param arr key value pairs
	 * @return Map key:value
	 */
	public static final Map<Object,Object> addToMap(Map<? super Object,? super Object> map, Object ... arr) {
		for(int i=1; i<arr.length; i+=2){
			map.put(arr[i-1], arr[i]);
		}
		return map;
	}	
	
	@SafeVarargs
	/** add varargs to List
	 * 
	 * @param arr
	 * @return
	 */
	public static final <T> List<T> addToList(List<T> list,T ... arr){
		for(T t:arr) list.add(t);
		return list;
	}
	
	
	@SuppressWarnings("unchecked")
	/** Generate query for "IN" operator : " IN(v1,v2,..)"
	 * 
	 * @param values for IN(...)
	 * @return Query object
	 */
	public static final Query queryIn(Object ...values){
		
		if(values.length == 0 && values[0] instanceof List)
			return joinValues(" IN(", (List<Object>) values[0], ",", ") ");
		
		return joinValues(" IN(", toList(values), ",", ") ");
	}

	/** Join multiple queries with glue/delimiter between.
	 * 
	 * @param queries
	 * @param glue
	 * @return new joined query or an empty Query if [queries] is empty
	 */
	public static final Query join(List<Query> queries, String glue){		
		return join(null,queries, glue, null);
	}
	
	/** Join multiple queries with glue/delimiter between. Also add a prefix if [queries] is not empty.
	 * 
	 * @param prefix
	 * @param queries
	 * @param glue
	 * @return new joined query or an empty Query if [queries] is empty
	 */
	public static final Query join(String prefix, List<Query> queries, String glue){
		return join(prefix,queries, glue, null);
	}
	
	/** Join multiple queries with glue/delimiter between. Also add a prefix and suffix if [queries] is not empty.
	 * 
	 * @param prefix
	 * @param queries
	 * @param glue
	 * @param suffix
	 * @return new joined query or an empty Query if [queries] is empty
	 */
	public static final Query join(String prefix, List<Query> queries, String glue, String suffix){
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
	
	/** Join multiple values with glue/delimiter between. Also add a prefix and suffix if [values] is not empty.
	 * 
	 * @param values
	 * @param glue
	 * @return new joined query or an empty Query if [values] is empty
	 */
	public static final Query joinValues(List<? extends Object> values, String glue){		
		return joinValues(null,values, glue, null);
	}
	
	/** Join multiple values with glue/delimiter between. Also add a prefix and suffix if [values] is not empty.
	 * 
	 * @param prefix
	 * @param values
	 * @param glue
	 * @return new joined query or an empty Query if [values] is empty
	 */
	public static final Query joinValues(String prefix, List<? extends Object> values, String glue){
		return joinValues(prefix,values, glue, null);
	}
	
	/** Join multiple values with glue/delimiter between. Also add a prefix and suffix if [values] is not empty.
	 * 
	 * @param prefix
	 * @param values
	 * @param glue
	 * @param suffix
	 * @return new joined query or an empty Query if [values] is empty
	 */
	public static final Query joinValues(String prefix, List<? extends Object> values, String glue, String suffix){
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

    @SuppressWarnings("unchecked")
    /** Utility that fills a java.util.Map based tree of values based on the supplied data List.<br>
     * It creates a new Map base tree level inside for each object in the list except for the last one(that one is added as is). 
     * 
     * @param map map that is being updated
     * @param row current row used to fill the map
     */
	public static void addRowToTree(Map<Object,Object> map, List<Object> row){
    	Map<Object, Object> current = map;
    	
    	int limit = row.size()-2;
    	int index = 0;
    	Object key = null;

    	while(index < limit){
    		key = row.get(index);
        	
    		Map<Object,Object> in  = (Map<Object, Object>) current.get(key);
        	if(in == null){
        		in = new HashMap<Object, Object>();
        		current.put(key, in);
        	}
        	current = in;
        	
        	index ++;
    	}
       	current.put(row.get(index), row.get(index+1));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })

    /** Utility that fills a java.util.Map based tree of values based on the supplied data and columns to be used
     * 
     * @param map to fill
     * @param row data
     * @param columns to use
     */
	protected static void addRowToTree(Map map, Map row, String ...columns){
    	Map<Object, Object> current = map;
        
    	int limit = columns.length-2;
    	int index = 0;
    	Object key = null;

    	while(index < limit){
    		key = row.get(columns[index]);
        	
    		Map<Object,Object> in  = (Map<Object, Object>) current.get(key);
        	if(in == null){
        		in = new HashMap<Object, Object>();
        		current.put(key, in);
        	}
        	current = in;
        	
    		index ++;
    	}
       	current.put(row.get(columns[index]), row.get(columns[index+1]));
    }

}
