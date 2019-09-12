package hr.hrg.hipster.sql;

import java.sql.*;
import java.util.*;

/** Short lived throw away instance that can be used per thread(sql connection) and discarded. 
 * You should not share instance between threads, although the worst that can happen is wrong query reported
 * in case of error, because lastQuery and lastPrepared are instance variables.
 * */
@SuppressWarnings("rawtypes")
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
	public int oneInt(Object ...sql){
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
    
    @Override
    public <T> T one(ICustomType<T> reader, Object... sql) {
        try(Result res = new Result(this);){
        	res.executeQuery(sql);

        	if(res.next()) {        		
        		return reader.get(res.getResultSet(), 1);
        	}
        	return null;

        } catch (SQLException e) {
        	throw new HipsterSqlException(this, "failed reading value", e);
		}
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
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> void rowsVisit(Object sql, T visitor) {
		Class<? extends Object> clazz = visitor.getClass();
		IResultFwdVisitor<T> handler = (IResultFwdVisitor<T>) hipster.getVisitorSource().getFor(clazz);
		if(handler == null){
			Class<?>[] interfaces = clazz.getInterfaces();
			if(interfaces.length == 1){
				handler = (IResultFwdVisitor<T>) hipster.getVisitorSource().getOrCreate(interfaces[0]);
				// register the implementation to the same handler so next time it is found on first try
				if(handler != null) hipster.getVisitorSource().registerFor(handler, clazz);
			}
		}

		if(handler == null) throw new HipsterSqlException(this, "Visitor handler not found for "+clazz.getName(), null);
    	rowsVisitFwd(sql, handler, visitor);
    }
    
    @Override
    public <T> void rowsVisitFwd(Object sql, IResultFwdVisitor<T> visitor, T fwd) {
    	
    	Object[] sqlArr = prepEntityQuery(Arrays.asList(new QueryLiteral(visitor.getColumnNamesStr())), null, sql);
    	
    	boolean autoCommit = false;
    	try {
    		// postgres does not use cursor if autoCommit is on
    		autoCommit = sqlConnection.getAutoCommit();
    		sqlConnection.setAutoCommit(false);
    	} catch (SQLException e) {
    		throw new HipsterSqlException(this, "autoCommit", e);
    	}
    	
    	try(Result res = new Result(this);){
    		res.setFetchSize(512);
    		
    		res.executeQuery(sqlArr);
    		
    		while(res.next()){
    			visitor.visitResult(res.getResultSet(), fwd);
    		}
    	}catch (Exception e) {
    		throw new HipsterSqlException(this, "visit failed", e);
    	}finally {
    		try{
    			sqlConnection.setAutoCommit(autoCommit);
    		} catch (SQLException e) {
    			throw new HipsterSqlException(this, "autoCommit", e);
    		}
    	}
    }

    @Override
    public void rowsVisitResult(Object sql, IResultSetVisitor visitor) {

        boolean autoCommit = false;
        try {
        	// postgres does not use cursor if autoCommit is on
			autoCommit = sqlConnection.getAutoCommit();
			sqlConnection.setAutoCommit(false);
		} catch (SQLException e) {
			throw new HipsterSqlException(this, "autoCommit", e);
		}

        try(Result res = new Result(this);){
        	res.setFetchSize(512);

        	res.executeQuery(sql);

	        while(res.next()){
	            visitor.visitResult(res.getResultSet());
	        }
        }catch (Exception e) {
        	throw new HipsterSqlException(this, "visit failed", e);
        }finally {
        	try{
        		sqlConnection.setAutoCommit(autoCommit);
    		} catch (SQLException e) {
    			throw new HipsterSqlException(this, "autoCommit", e);
    		}
		}
    }

    public <E extends IQueryPart> Object[] prepEntityQuery(List<E> list, IQueryLiteral table, Object... sql) {

    	if(list == null || list.isEmpty()) return sql; // no need to inject column names
    	
    	Query columnsQuery = QueryUtil.join(list, ",");
    	if(sql.length == 0) {
			return new Object[]{new Query(" SELECT ",columnsQuery," FROM ",table)};
		}
    	
    	Object[] newSql = null;
    	if(sql.length == 1 && sql[0] instanceof Query){
    		newSql = ((Query)sql[0]).parts.toArray();
    	}else{
    		newSql = new Object[sql.length];
    		System.arraycopy(sql, 0, newSql, 0, sql.length);
    	}
    	if(newSql[0] instanceof String){
    		String first = ((String) newSql[0]).toLowerCase();
    		if(first.startsWith("from ") || first.startsWith(" from ")){
    			return new Object[] {new Query(" SELECT ",columnsQuery," "), new Query(newSql)};
    		}
    		if(table != null && !( first.startsWith("select ") || first.startsWith(" select "))){
    			return new Object[] {new Query(" SELECT ",columnsQuery," FROM ",table," "), new Query(newSql)};
    		}
    	}
    	return sql;
    }

    @Override
    public <T> T entity(Class<T> clazz, Object... sql) {
    	return entity(hipster.getReaderSource().getOrCreate(clazz), sql);
    }

    @Override
    public <T,E extends BaseColumnMeta> T entity(IReadMeta<T, E> reader, Object... sql) {
    	sql = prepEntityQuery(reader.getColumns(), reader.getTable(), sql);
    	
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
    public <T,E extends BaseColumnMeta> List<T> entities(IReadMeta<T, E> reader, Object... sql) {
    	
    	sql = prepEntityQuery(reader.getColumns(), reader.getTable(), sql);

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
    	return column((ICustomType<T>)hipster.getTypeSource().getForRequired(clazz), sql);
    }

    @Override
    public <T> List<T> column(ICustomType<T> reader, Object... sql) {
    	
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
    public <T,E extends BaseColumnMeta> List<T> entitiesLimit(IReadMeta<T, E> reader, int offset, int limit, Object... sql){
    	sql = prepEntityQuery(reader.getColumns(), reader.getTable(), sql); 
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
	public int update(Object ...sql){
    	try(Result res = new Result(this);){    		
    		return res.update(sql);
    	}
    }

    /* (non-Javadoc)
	 * @see hr.hrg.hipster.sql.HipsterConnection#insert(hr.hrg.hipster.sql.Query)
	 */
    @Override
    public Object insert(Query sql){
    	try(Result res = new Result(this);){
    		res.executeUpdate(sql);
    		return res.fetchLong();
    	}
    }
    
    @SuppressWarnings("unchecked")
	@Override
	public <T> T insert(Class<T> primaryColumnType, Query sql){
    	
    	ICustomType<?> resultGetter = hipster.getTypeSource().getForRequired(primaryColumnType);
        
    	try(Result res = new Result(this);){
        	res.executeUpdate(sql);
        	
        	if(res.next())
        		return (T) resultGetter.get(res.getResultSet(), 1);
        	else
        		return null;
        	
        } catch (SQLException e) {
        	throw new HipsterSqlException(this, "Problem inserting and retreiving id("+primaryColumnType.getName()+")", e);
        }
    }

	public String lastQueryInfo() {
		if(lastQuery != null) return lastQuery.toString();
		if(lastPrepared != null) return lastPrepared.toString();
		return null;
	}	
	    
}
