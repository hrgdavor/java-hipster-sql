package hr.hrg.hipster.sql;

import hr.hrg.hipster.query.*;

public class HipsterSqlException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	private Query lastQuery;

	public HipsterSqlException(Query lastQuery, String message, Throwable cause){
		super(message+": "+lastQuery, cause);
		this.lastQuery = lastQuery;
	}

	public HipsterSqlException(IHipsterConnection conn, String message, Throwable cause){
		super(message+": "+conn.getLastQuery(), cause);
		this.lastQuery = conn.getLastQuery();
	}
	
	public Query getLastQuery() {
		return lastQuery;
	}
	
}
