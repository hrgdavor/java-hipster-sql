package hr.hrg.hipstersql;

import java.util.*;
import static hr.hrg.hipstersql.QueryUtil.*;

public class QueryUtilShort {

	/** Short method name to build query inline */
	public static final Query q(Object ... q) {
		return new Query(q);
	}
	
	/** Short method name to build PreparedQuery inline */
	public static final PreparedQuery prep(String query, List<Object> params) {
		return new PreparedQuery(query, params);
	}
	
	/** Short method name to build PreparedQuery inline */
	public static final PreparedQuery prep(String query, Object ...params) {
		return new PreparedQuery(query, params);
	}

	/** Shortcut for {@link QueryUtil#queryIn(Object...)}
	 * @see {@link QueryUtil#queryIn(Object...)}
	 */
	public static final Query qIn(Object ...values){
		return queryIn(values);
	}
	
}
