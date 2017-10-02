package hr.hrg.hipster.sql;

public class HipsterSqlException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	private Query lastQuery;
	private Query lastPrepared;

	public HipsterSqlException(IHipsterConnection conn, String message, Throwable cause){
		super(message, cause);
		this.lastQuery = conn.getLastQuery();
		this.lastPrepared = conn.getLastQuery();
	}
	
	public Query getLastQuery() {
		return lastQuery;
	}
	
	public Query getLastPrepared() {
		return lastPrepared;
	}
}
