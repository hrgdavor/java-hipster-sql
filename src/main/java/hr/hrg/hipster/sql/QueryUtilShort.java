package hr.hrg.hipster.sql;

import static hr.hrg.hipster.sql.QueryUtil.*;

import java.util.*;

public class QueryUtilShort {

	/** Short method name to build query inline 
	 * 
	 * @param sql varags query
	 * @return Query object
	 */
	public static final Query q(Object ... sql) {
		return new Query(sql);
	}
	
	/** Short method name to build PreparedQuery inline 
	 * 
	 * @param query string with placeholders for values
	 * @param params params to put instead placeholders
	 * @return PreparedQuery object
	 */
	public static final PreparedQuery prep(String query, List<Object> params) {
		return new PreparedQuery(query, params);
	}
	
	/** Short method name to build PreparedQuery inline with syntax like the java.sql.PreparedStatement
	 * 
	 * @param query string with placeholders for values
	 * @param params params to put instead placeholders
	 * @return the PreparedQuery
	 */
	public static final PreparedQuery prep(String query, Object ...params) {
		return new PreparedQuery(query, params);
	}

	/** Shortcut for {@link QueryUtil#queryIn(Object...)}
	 * @see QueryUtil#queryIn(Object...)
	 * 
	 * @param values for IN(...)
	 * @return Query object
	 */
	public static final Query qIn(Object ...values){
		return queryIn(values);
	}
	
}
