package hr.hrg.hipster.sql;

import java.sql.*;
import java.util.*;

/** Short lived throw away instance that can be used per thread(sql connection) and discarded. 
 * You should not share instance between threads, although the worst that can happen is wrong query reported
 * in case of error, because lastQuery and lastPrepared are instance variables.
 * */
public class HipsterConnectionImpl implements IHipsterConnection {
	
	protected HipsterSql hipster;
	protected Connection sqlConnection;
	protected Query lastQuery;
	protected PreparedQuery lastPrepared;
	
	
	public HipsterConnectionImpl(HipsterSql hipster, Connection sqlConnection) {
		this.hipster = hipster;
		this.sqlConnection = sqlConnection;
	}
	
	/* (non-Javadoc)
	 * @see hr.hrg.hipster.sql.HipsterConnection#getConnection()
	 */
	@Override
	public Connection getConnection() {
		return sqlConnection;
	}
	
	/* (non-Javadoc)
	 * @see hr.hrg.hipster.sql.HipsterConnection#getLastQuery()
	 */
	@Override
	public Query getLastQuery() {
		return lastQuery;
	}
	
	/* (non-Javadoc)
	 * @see hr.hrg.hipster.sql.HipsterConnection#getLastPrepared()
	 */
	@Override
	public PreparedQuery getLastPrepared() {
		return lastPrepared;
	}
	
	/* (non-Javadoc)
	 * @see hr.hrg.hipster.sql.HipsterConnection#getHipster()
	 */
	@Override
	public HipsterSql getHipster() {
		return hipster;
	}
	
	/* (non-Javadoc)
	 * @see hr.hrg.hipster.sql.HipsterConnection#getSqlConnection()
	 */
	@Override
	public Connection getSqlConnection() {
		return sqlConnection;
	}
	
    /* (non-Javadoc)
	 * @see hr.hrg.hipster.sql.HipsterConnection#oneObj(java.lang.Object)
	 */
    @Override
	public Object oneObj(Object ...sql){
        try(Result res = new Result(this);){
        	res.executeQuery(sql);

	        List<Object> row = res.fetchRow();
	        if(row == null) return null;
	        return row.get(0);
        }
    }
    
    /* (non-Javadoc)
	 * @see hr.hrg.hipster.sql.HipsterConnection#oneString(java.lang.Object)
	 */
    @Override
	public String oneString(Object ...sql){
    	Object obj = oneObj(sql);
    	return obj == null ? null: obj.toString();
    }

    /* (non-Javadoc)
	 * @see hr.hrg.hipster.sql.HipsterConnection#one(java.lang.Object)
	 */
    @Override
	public int one(Object ...sql){
    	Object obj = oneObj(sql);
    	return obj == null ? 0: ((Number)obj).intValue();
    }

    /* (non-Javadoc)
	 * @see hr.hrg.hipster.sql.HipsterConnection#oneLong(java.lang.Object)
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
	 * @see hr.hrg.hipster.sql.HipsterConnection#row(java.lang.Object)
	 */
    @Override
	public Map<Object, Object> row(Object ...sql){
        try(Result res = new Result(this);){
        	res.executeQuery(sql);
        	return res.fetchAssoc();
        }        
    }

    /* (non-Javadoc)
	 * @see hr.hrg.hipster.sql.HipsterConnection#rowsLimit(int, int, java.lang.Object)
	 */
    @Override
	public List<Map<Object, Object>> rowsLimit(int offset, int limit, Object ...sql){
    	return rows(new Query(sql).append(new Query(" LIMIT "+limit+" OFFSET "+offset)));
    }

    @Override
	public List<Map<Object, Object>> rows(Object ...sql){
        List<Map<Object, Object>> rows = new ArrayList<>();
        try(Result res = new Result(this);){
        	res.executeQuery(sql);

	        Map<Object, Object> row;
	        while((row = res.fetchAssoc()) != null){
	            rows.add(row);
	        }
        }

        return rows;
    }
    
    public <T,E extends IColumnMeta> Object[] prepEntityQuery(IReadMeta<T, E> reader, Object... sql) {
    	Object[] newSql = new Object[sql.length];
    	System.arraycopy(sql, 0, newSql, 0, sql.length);
    	if(sql[0] instanceof String){
    		String first = ((String) sql[0]).toLowerCase();
    		if(first.startsWith("from ") || first.startsWith(" from ")){
    			newSql[0] = " SELECT "+reader.getColumnNamesStr()+" "+sql[0];
    		}
    		return newSql;
    	}
    	return sql;
    }
    
    @Override
    public <T> T entity(Class<T> clazz, Object... sql) {
    	return entity(hipster.getReaderSource().getOrCreate(clazz), sql);
    }

    @Override
    public <T,E extends IColumnMeta> T entity(IReadMeta<T, E> reader, Object... sql) {
    	sql = prepEntityQuery(reader, sql);
    	
    	try(Result res = new Result(this);){
        	res.executeQuery(sql);
        	return res.fetchEntity(reader);
        }
    }

    @Override
    public <T> List<T> entities(Class<T> clazz, Object... sql) {
    	return entities(hipster.getReaderSource().getOrCreate(clazz), sql);
    }

    @Override
    public <T,E extends IColumnMeta> List<T> entities(IReadMeta<T, E> reader, Object... sql) {
    	
    	sql = prepEntityQuery(reader, sql);

    	List<T> ret = new ArrayList<>();
    	T entity = null;
    	
    	try(Result res = new Result(this);){
        	res.executeQuery(sql);
        	while((entity = res.fetchEntity(reader)) != null){
        		ret.add(entity);
        	}
        }

    	return ret;
    }

	@Override
	@SuppressWarnings("unchecked")
    public <T> List<T> column(Class<T> clazz, Object... sql) {
    	return column((IResultGetter<T>)hipster.getResultGetterSource().getForRequired(clazz), sql);
    }

    @Override
    public <T> List<T> column(IResultGetter<T> reader, Object... sql) {
    	
    	List<T> ret = new ArrayList<>();
    	
    	try(Result res = new Result(this);){
        	res.executeQuery(sql);
        	while(res.next()){
        		try {
					ret.add(reader.get(res.getResultSet(), 1));
				} catch (SQLException e) {
					throw new HipsterSqlException(this, "reading column", e);
				}
        	}
        }

    	return ret;
    }
    
    @Override
    public <T> List<T> entitiesLimit(Class<T> clazz, int offset, int limit, Object... sql) {
    	return entitiesLimit(hipster.getReaderSource().getOrCreate(clazz), offset, limit, sql);    
    }

    @Override
    public <T,E extends IColumnMeta> List<T> entitiesLimit(IReadMeta<T, E> reader, int offset, int limit, Object... sql){
    	sql = prepEntityQuery(reader, sql); 
    	return entities(reader,new Query(sql).append(new Query(" LIMIT "+limit+" OFFSET "+offset)));
    }


    @Override
	public List<Object> column(Object ...sql){
        List<Object> column = new ArrayList<>();
        try(Result res = new Result(this);){
        	res.executeQuery(sql);

	        List<Object> row;
	        while((row = res.fetchRow()) != null){
	            column.add(row.get(0));
	        }
        }
        return column;
    }	

    @Override
    public List<Double> columnDouble(Object... sql) {
    	List<Double> column = new ArrayList<>();
    	Double val;
    	try(Result res = new Result(this);){
    		res.executeQuery(sql);
    		
    		while((val = res.fetchDouble()) != null){
    			column.add(val);
    		}
    	}
    	return column;
    }
    
    @Override
    public List<Integer> columnInteger(Object... sql) {
    	List<Integer> column = new ArrayList<>();
    	Integer val;
    	try(Result res = new Result(this);){
    		res.executeQuery(sql);
    		
    		while((val = res.fetchInteger()) != null){
    			column.add(val);
    		}
    	}
    	return column;
    }
    
    @Override
    public List<Long> columnLong(Object... sql) {
    	List<Long> column = new ArrayList<>();
    	Long val;
    	try(Result res = new Result(this);){
    		res.executeQuery(sql);
    		
    		while((val = res.fetchLong()) != null){
    			column.add(val);
    		}
    	}
    	return column;
    }
    
    @Override
    public List<String> columnString(Object... sql) {
    	List<String> column = new ArrayList<>();
    	String val;
    	try(Result res = new Result(this);){
    		res.executeQuery(sql);
    		
    		while((val = res.fetchString()) != null){
    			column.add(val);
    		}
    	}
    	return column;
    }
    
    @Override
    public List<Float> columnFloat(Object... sql) {
        List<Float> column = new ArrayList<>();
        Float val;
        try(Result res = new Result(this);){
        	res.executeQuery(sql);

	        while((val = res.fetchFloat()) != null){
	            column.add(val);
	        }
        }
        return column;
    }
    

    /* (non-Javadoc)
	 * @see hr.hrg.hipster.sql.HipsterConnection#treeWithValue(java.lang.Object)
	 */
    @Override
	public Map<Object,Object> treeWithValue(Object ...sql){
        Map<Object, Object> map = new HashMap<>();
        try(Result res = new Result(this);){
        	res.executeQuery(sql);
        	List<Object> row;
        	while((row = res.fetchRow()) != null){
        		QueryUtil.addRowToTree(map, row);
        	}        	
        }        
        return map;
    }

    /* (non-Javadoc)
	 * @see hr.hrg.hipster.sql.HipsterConnection#treeWithRow(hr.hrg.hipster.sql.Query, java.lang.String)
	 */
    @Override
	@SuppressWarnings("rawtypes")
	public Map<Object, Map<Object, Object>> treeWithRow(Query sql, String ...columns) {
        Map<Object, Map<Object, Object>> map = new HashMap<Object,Map<Object,Object>>();
        try(Result res = new Result(this);){
        	res.executeQuery(sql);

	        Map row;
	        while((row = res.fetchAssoc()) != null){
	            QueryUtil.addRowToTree(map, row, columns);
	        }
        }
        
        return map;
	}

    /* (non-Javadoc)
	 * @see hr.hrg.hipster.sql.HipsterConnection#update(java.lang.Object)
	 */
    @Override
	public int update(Object sql){
    	try(Result res = new Result(this);){    		
    		return res.update(sql);
    	}
    }

    /* (non-Javadoc)
	 * @see hr.hrg.hipster.sql.HipsterConnection#insert(hr.hrg.hipster.sql.Query)
	 */
    @Override
	public Object insert(Query sql){
        return oneObj(sql);
    }

	public String lastQueryInfo() {
		if(lastQuery != null) return lastQuery.toString();
		if(lastPrepared != null) return lastPrepared.toString();
		return null;
	}	
	    
}
