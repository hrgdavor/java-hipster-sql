package hr.hrg.hipster.sql;

public class HipsterSqlException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	private Query lastQuery;

	public HipsterSqlException(Query lastQuery, String message, Throwable cause){
		super(message+": "+lastQuery, cause);
		this.lastQuery = lastQuery;
	}

	public HipsterSqlException(IHipsterConnection conn, String message, Throwable cause){
		super(message+": "+or( conn.getLastQuery(),conn.getLastQuery()), cause);
		this.lastQuery = conn.getLastQuery();
	}
	
	private static final Object or(Object o1, Object o2){
		if(o1 != null) return o1;
		return o2;
	}
	
	public Query getLastQuery() {
		return lastQuery;
	}
	
}
