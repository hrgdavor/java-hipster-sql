package hr.hrg.hipstersql;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Short lived throw away instance that can be used per thread(sql connection) and discarded. */
public class HipsterConnection {
	
	protected HipsterSql hipster;
	protected Connection sqlConnection;
	protected Query lastQuery;
	protected PreparedQuery lastPrepared;
	
	
	public HipsterConnection(HipsterSql hipster, Connection sqlConnection) {
		this.hipster = hipster;
		this.sqlConnection = sqlConnection;
	}
	
	public Connection getConnection() {
		return sqlConnection;
	}
	
	public Query getLastQuery() {
		return lastQuery;
	}
	
	public PreparedQuery getLastPrepared() {
		return lastPrepared;
	}
	
	public HipsterSql getHipster() {
		return hipster;
	}
	
	public Connection getSqlConnection() {
		return sqlConnection;
	}
	
	/** Get first value as int from first row and first column. <br/>
     * Useful for counting and other queries that return single int value.<br/>*/
    public int one(Object ...sql){
        try(Result res = new Result(this, sql);){
        	res.query();

        	List<Object> row = res.fetchRow();
        	if(row == null) return 0;
        	Number number = (Number)row.get(0);
        	return number == null ? 0:number.intValue();
        }
    }

    /** Get first value as long from first row and first column. <br/>
     */
    public long oneLong(Object ...sql){
        try(Result res = new Result(this, sql);){
        	res.query();

	    	List<Object> row = res.fetchRow();
	    	if(row == null) return 0;
	    	Number number = (Number)row.get(0);
	    	return number == null ? 0:number.longValue();
        }
    }
    
    /** Get first value as long from first row and first column. <br/>
     */
    public String oneString(Object ...sql){
        try(Result res = new Result(this, sql);){
        	res.query();

	        List<Object> row = res.fetchRow();
	        if(row == null) return null;
	        return row.get(0).toString();
        }
    }
    
    /** Get first value from first row and first column. <br/>
     * Useful for counting and other queries that return single value.<br/>*/
    public Object oneObj(Object ...sql){
        try(Result res = new Result(this, sql);){
        	res.query();

	        List<Object> row = res.fetchRow();
	        if(row == null) return null;
	        return row.get(0);
        }
    }

    /**
     * Get single row. 
     */
    public Map<Object, Object> row(Object ...sql){
        try(Result res = new Result(this, sql);){
        	res.query();
        	return res.fetchAssoc();
        }        
    }

    /** return result as rows, but react on Thread.interrupt */
    public List<Map<Object, Object>> rowsInterruptible(Object ...sql) throws InterruptedException{
        List<Map<Object, Object>> rows = new ArrayList<>();
        try(Result res = new Result(this, sql);){
        	res.query();

	        Map<Object, Object> row;
	        while((row = res.fetchAssoc()) != null){
	        	if(Thread.interrupted()) throw new InterruptedException("Iterrupted while reading rows "+res.query);
	            rows.add(row);
	        }
        }
        return rows;    	
    }

    public List<Map<Object, Object>> rowsLimit(int offset, int limit, Object ...sql){
    	return rows(new Query(sql).append(new Query(" LIMIT "+limit+" OFFSET "+offset)));
    }

    public List<Map<Object, Object>> rows(Object ...sql){
        List<Map<Object, Object>> rows = new ArrayList<>();
        try(Result res = new Result(this, sql);){
        	res.query();

	        Map<Object, Object> row;
	        while((row = res.fetchAssoc()) != null){
	            rows.add(row);
	        }
        }

        return rows;
    }

    public List<Object> column(Object ...sql){
        List<Object> column = new ArrayList<Object>();
        try(Result res = new Result(this, sql);){
        	res.query();

	        List<Object> row;
	        while((row = res.fetchRow()) != null){
	            column.add(row.get(0));
	        }
        }
        return column;
    }	


    public Map<Object,Object> treeWithValue(Object ...sql){
        Map<Object, Object> map = new HashMap<>();
        try(Result res = new Result(this, sql);){
        	res.query();
        	List<Object> row;
        	while((row = res.fetchRow()) != null){
        		QueryUtil.addRowToTree(map, row);
        	}        	
        }        
        return map;
    }

    @SuppressWarnings("rawtypes")
	public Map<Object, Map<Object, Object>> treeWithRow(Query sql, String ...columns) {
        Map<Object, Map<Object, Object>> map = new HashMap<Object,Map<Object,Object>>();
        try(Result res = new Result(this, sql);){
        	res.query();

	        Map row;
	        while((row = res.fetchAssoc()) != null){
	            QueryUtil.addRowToTree(map, row, columns);
	        }
        }
        
        return map;
	}

    /** Execute update and return number of affected rows */
    public int update(Object sql){
    	try(Result res = new Result(this, sql);){    		
    		return res.updateAndClose();
    	}
    }

    public Object insert(Query sql){
        return oneObj(sql);
    }	
	    
}
