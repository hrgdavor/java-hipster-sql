package hr.hrg.hipster.sql;

public class HipsterSqlException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	private Query lastQuery;
	private PreparedQuery lastPrepared;

	public HipsterSqlException(IHipsterConnection conn, String message, Throwable cause){
		super(message+": "+or( conn.getLastQuery(),conn.getLastPrepared()), cause);
		this.lastQuery = conn.getLastQuery();
		this.lastPrepared = conn.getLastPrepared();
	}
	
	private static final Object or(Object o1, Object o2){
		if(o1 != null) return o1;
		return o2;
	}
	
	public Query getLastQuery() {
		return lastQuery;
	}
	
	public PreparedQuery getLastPrepared() {
		return lastPrepared;
	}
}
