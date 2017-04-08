package hr.hrg.hipstersql;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Short lived throw away instance that can be used per thread(sql connection) and discarded. */
public class HipsterConnectionImpl implements HipsterConnection {
	
	protected HipsterSql hipster;
	protected Connection sqlConnection;
	protected Query lastQuery;
	protected PreparedQuery lastPrepared;
	
	
	public HipsterConnectionImpl(HipsterSql hipster, Connection sqlConnection) {
		this.hipster = hipster;
		this.sqlConnection = sqlConnection;
	}
	
	/* (non-Javadoc)
	 * @see hr.hrg.hipstersql.HipsterConnection#getConnection()
	 */
	@Override
	public Connection getConnection() {
		return sqlConnection;
	}
	
	/* (non-Javadoc)
	 * @see hr.hrg.hipstersql.HipsterConnection#getLastQuery()
	 */
	@Override
	public Query getLastQuery() {
		return lastQuery;
	}
	
	/* (non-Javadoc)
	 * @see hr.hrg.hipstersql.HipsterConnection#getLastPrepared()
	 */
	@Override
	public PreparedQuery getLastPrepared() {
		return lastPrepared;
	}
	
	/* (non-Javadoc)
	 * @see hr.hrg.hipstersql.HipsterConnection#getHipster()
	 */
	@Override
	public HipsterSql getHipster() {
		return hipster;
	}
	
	/* (non-Javadoc)
	 * @see hr.hrg.hipstersql.HipsterConnection#getSqlConnection()
	 */
	@Override
	public Connection getSqlConnection() {
		return sqlConnection;
	}
	
    /* (non-Javadoc)
	 * @see hr.hrg.hipstersql.HipsterConnection#oneObj(java.lang.Object)
	 */
    @Override
	public Object oneObj(Object ...sql){
        try(Result res = new Result(this, sql);){
        	res.query();

	        List<Object> row = res.fetchRow();
	        if(row == null) return null;
	        return row.get(0);
        }
    }
    
    /* (non-Javadoc)
	 * @see hr.hrg.hipstersql.HipsterConnection#oneString(java.lang.Object)
	 */
    @Override
	public String oneString(Object ...sql){
    	Object obj = oneObj(sql);
    	return obj == null ? null: obj.toString();
    }

    /* (non-Javadoc)
	 * @see hr.hrg.hipstersql.HipsterConnection#one(java.lang.Object)
	 */
    @Override
	public int one(Object ...sql){
    	Object obj = oneObj(sql);
    	return obj == null ? 0: ((Number)obj).intValue();
    }

    /* (non-Javadoc)
	 * @see hr.hrg.hipstersql.HipsterConnection#oneLong(java.lang.Object)
	 */
    @Override
	public long oneLong(Object ...sql){
    	Object obj = oneObj(sql);
    	return obj == null ? 0: ((Number)obj).longValue();
    }

    @Override
	public double oneDouble(Object ...sql){
    	Object obj = oneObj(sql);
    	return obj == null ? 0: ((Number)obj).doubleValue();
    }
    
    /* (non-Javadoc)
	 * @see hr.hrg.hipstersql.HipsterConnection#row(java.lang.Object)
	 */
    @Override
	public Map<Object, Object> row(Object ...sql){
        try(Result res = new Result(this, sql);){
        	res.query();
        	return res.fetchAssoc();
        }        
    }

    /* (non-Javadoc)
	 * @see hr.hrg.hipstersql.HipsterConnection#rowsInterruptible(java.lang.Object)
	 */
    @Override
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

    /* (non-Javadoc)
	 * @see hr.hrg.hipstersql.HipsterConnection#rowsLimit(int, int, java.lang.Object)
	 */
    @Override
	public List<Map<Object, Object>> rowsLimit(int offset, int limit, Object ...sql){
    	return rows(new Query(sql).append(new Query(" LIMIT "+limit+" OFFSET "+offset)));
    }

    /* (non-Javadoc)
	 * @see hr.hrg.hipstersql.HipsterConnection#rows(java.lang.Object)
	 */
    @Override
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

    /* (non-Javadoc)
	 * @see hr.hrg.hipstersql.HipsterConnection#column(java.lang.Object)
	 */
    @Override
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


    /* (non-Javadoc)
	 * @see hr.hrg.hipstersql.HipsterConnection#treeWithValue(java.lang.Object)
	 */
    @Override
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

    /* (non-Javadoc)
	 * @see hr.hrg.hipstersql.HipsterConnection#treeWithRow(hr.hrg.hipstersql.Query, java.lang.String)
	 */
    @Override
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

    /* (non-Javadoc)
	 * @see hr.hrg.hipstersql.HipsterConnection#update(java.lang.Object)
	 */
    @Override
	public int update(Object sql){
    	try(Result res = new Result(this, sql);){    		
    		return res.update();
    	}
    }

    /* (non-Javadoc)
	 * @see hr.hrg.hipstersql.HipsterConnection#insert(hr.hrg.hipstersql.Query)
	 */
    @Override
	public Object insert(Query sql){
        return oneObj(sql);
    }	
	    
}
