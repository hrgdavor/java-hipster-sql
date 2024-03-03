package hr.hrg.hipster.sql;

import java.sql.*;
import java.util.*;

import hr.hrg.hipster.entity.*;
import hr.hrg.hipster.query.*;
import hr.hrg.hipster.type.*;
import hr.hrg.hipster.visitor.*;

/** Short lived throw away instance that can be used per thread(sql connection) and discarded. 
 * You should not share instance between threads, although the worst that can happen is wrong query reported
 * in case of error, because lastQuery and lastPrepared are instance variables.
 * */
@SuppressWarnings("rawtypes")
public class HipsterConnectionImpl implements IHipsterConnection {
	
	protected HipsterSql hipster;
	protected Connection sqlConnection;
	protected Query lastQuery;
	
	
	public HipsterConnectionImpl(HipsterSql hipster, Connection sqlConnection) {
		this.hipster = hipster;
		this.sqlConnection = sqlConnection;
	}
	
	@Override
	public IQueryLogger getQueryLogger() {
		return hipster.getQueryLogger();
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
    
//    @Override
//    @SuppressWarnings("unchecked")
//    public <T> void rowsVisit(Object sql, T visitor) {
//		Class<? extends Object> clazz = visitor.getClass();
//		IResultFwdVisitor<T> handler = (IResultFwdVisitor<T>) hipster.getVisitorSource().getFor(clazz);
//		if(handler == null){
//			Class<?>[] interfaces = clazz.getInterfaces();
//			if(interfaces.length == 1){
//				handler = (IResultFwdVisitor<T>) hipster.getVisitorSource().getOrCreate(interfaces[0]);
//				// register the implementation to the same handler so next time it is found on first try
//				if(handler != null) hipster.getVisitorSource().registerFor(handler, clazz);
//			}
//		}
//
//		if(handler == null) throw new HipsterSqlException(this, "Visitor handler not found for "+clazz.getName(), null);
//    	rowsVisitFwd(sql, handler, visitor);
//    }
//    
//    @Override
//    public <T> void rowsVisitFwd(Object sql, IResultFwdVisitor<T> visitor, T fwd) {
//    	
//    	Query query = prepEntityQuery(Arrays.asList(new QueryLiteral(visitor.getColumnNamesStr())), null, hipster.q(sql));
//    	
//    	boolean autoCommit = false;
//    	try {
//    		// postgres does not use cursor if autoCommit is on
//    		autoCommit = sqlConnection.getAutoCommit();
//    		sqlConnection.setAutoCommit(false);
//    	} catch (SQLException e) {
//    		throw new HipsterSqlException(this, "autoCommit", e);
//    	}
//    	
//    	try(Result res = new Result(this);){
//    		res.setFetchSize(512);
//    		
//    		res.executeQuery(query);
//    		
//    		while(res.next()){
//    			visitor.visitResult(res.getResultSet(), fwd);
//    		}
//    	}catch (Exception e) {
//    		throw new HipsterSqlException(this, "visit failed", e);
//    	}finally {
//    		try{
//    			sqlConnection.setAutoCommit(autoCommit);
//    		} catch (SQLException e) {
//    			throw new HipsterSqlException(this, "autoCommit", e);
//    		}
//    	}
//    }
//
//    @Override
//    public void rowsVisitResult(Object sql, IResultSetVisitor visitor) {
//
//        boolean autoCommit = false;
//        try {
//        	// postgres does not use cursor if autoCommit is on
//			autoCommit = sqlConnection.getAutoCommit();
//			sqlConnection.setAutoCommit(false);
//		} catch (SQLException e) {
//			throw new HipsterSqlException(this, "autoCommit", e);
//		}
//
//        try(Result res = new Result(this);){
//        	res.setFetchSize(512);
//
//        	res.executeQuery(sql);
//
//	        while(res.next()){
//	            visitor.visitResult(res.getResultSet());
//	        }
//        }catch (Exception e) {
//        	throw new HipsterSqlException(this, "visit failed", e);
//        }finally {
//        	try{
//        		sqlConnection.setAutoCommit(autoCommit);
//    		} catch (SQLException e) {
//    			throw new HipsterSqlException(this, "autoCommit", e);
//    		}
//		}
//    }

    public <E extends IQueryLiteral> Query prepEntityQuery(List<? extends ColumnMeta> list, IQueryLiteral table, Query sql) {

    	if(list == null || list.isEmpty()) return sql; // no need to inject column names
    	String str = sql.getQueryExpression().toString().toLowerCase();
    	if(str.startsWith("select ") || str.startsWith(" select ")) return sql;
    	
    	Query columnsQuery = hipster.q("SELECT ").addPartsList(",", list);

    	    	
		if(str.startsWith("from ") || str.startsWith(" from ")){
			sql.addAtBegining(columnsQuery);
		
		}else if(table != null &&(str.startsWith("where ") || str.startsWith(" where "))){
			columnsQuery.add(" FROM ", table).add(" ");
			sql.addAtBegining(columnsQuery);
		}
    	return sql;
    }

    @Override
    public <T> T entity(Class<T> clazz, Object... sql) {
    	return entity(hipster.getEntitySource().getForRequired(clazz), sql);
    }

    @Override
    public <T,ID> T entity(IEntityMeta<T, ID> reader, Object... sql) {
    	Query query = prepEntityQuery(reader.getColumns(), reader, hipster.q(sql));
    	
    	try(Result res = new Result(this);){
        	res.executeQuery(query);
        	return res.fetchEntity(reader);
        }
    }

    @Override
    public <T> List<T> entities(Class<T> clazz, Object... sql) {
    	return entities(hipster.getEntitySource().getForRequired(clazz), sql);
    }

    
	@Override
    public <T, ID> List<T> entities(IEntityMeta<T, ID> reader, Object... sql) {
    	
    	Query query = prepEntityQuery(reader.getColumns(), reader, hipster.q(sql));
    	List<T> ret = new ArrayList<>();
    	T entity = null;
    	
    	try(Result res = new Result(this);){
        	res.executeQuery(query);
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
    	return entitiesLimit(hipster.getEntitySource().getForRequired(clazz), offset, limit, sql);
    }

    @Override
    public <T,ID> List<T> entitiesLimit(IEntityMeta<T,ID> reader, int offset, int limit, Object... sql){
    	Query query  = prepEntityQuery(reader.getColumns(), reader, hipster.q(sql)); 
    	return entities(reader, query.add(" LIMIT "+limit+" OFFSET "+offset));
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
	 * @see hr.hrg.hipster.sql.HipsterConnection#treeWithRow(hr.hrg.hipster.sql.QueryOld, java.lang.String)
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
	public int update(Query sql){
    	try(Result res = new Result(this);){    		
    		return res.update(sql);
    	}
    }
    
    @Override
    public <T, ID> int update(IEntityMeta<T, ID> meta, IUpdatable mutable) {
		Query updateQuery = hipster.buildUpdate(
				meta, 
				q(meta.getPrimaryColumn(),"=",meta.entityGetPrimary((T)mutable)), 
				mutable
			);
		return update(updateQuery);
    }

    /* (non-Javadoc)
	 * @see hr.hrg.hipster.sql.HipsterConnection#insert(hr.hrg.hipster.sql.QueryOld)
	 */
    @Override
    public Object insert(Query sql){
    	try(Result res = new Result(this);){
    		res.executeUpdate(sql);
    		return res.fetchLong();
    	}
    }
    
    @Override
    public <T, ID> ID insert(IEntityMeta<T, ID> meta, IUpdatable mutable) {
		Query insertQuery = hipster.buildInsert(meta, mutable);
		return (ID) insert(meta.getPrimaryColumn().getType(), insertQuery);
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
		return null;
	}	

	@Override
	public Query q(Object... parts) {
		return hipster.q(parts);
	}
	
    @Override
    public void rowsVisitResult(Query sql, IResultSetVisitor visitor) {

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

    @Override
    public <T,ID> void rowsVisitEntity(IEntityMeta<T, ID> reader, Query filter, Lambda1<T> visitor) {
    	Query query = prepEntityQuery(reader.getColumns(), reader, filter);

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
    		
    		res.executeQuery(query);
   		
    		T entity = null;
    		while((entity = res.fetchEntity(reader)) != null){
    			visitor.run(entity);
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
    @SuppressWarnings("unchecked")
    public<T1> void rowsVisitResult(
    		ColumnMeta<T1> c1,
    		Query filter,
    		Lambda1<T1> visitor) {
    	
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
    		
    		ICustomType<T1> typeHandler1 = (ICustomType<T1>) c1.getTypeHandler();
    		
    		res.executeQuery(q("SELECT ",c1," FROM ", c1.getMeta()," ", filter));
    		
    		while(res.next()){
    			ResultSet rs = res.getResultSet();
    			T1 p1 = typeHandler1.get(rs, 1);
    			visitor.run(p1);
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
    @SuppressWarnings("unchecked")
    public<T1,T2> void rowsVisitResult(
    		ColumnMeta<T1> c1,
    		ColumnMeta<T2> c2,
    		Query filter,
    		Lambda2<T1,T2> visitor) {

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

        	ICustomType<T1> typeHandler1 = (ICustomType<T1>) c1.getTypeHandler();
        	ICustomType<T2> typeHandler2 = (ICustomType<T2>) c2.getTypeHandler();

        	res.executeQuery(q("SELECT ",c1,",",c2," FROM ", c1.getMeta()," ", filter));
	        
        	while(res.next()){
	        	ResultSet rs = res.getResultSet();
	        	T1 p1 = typeHandler1.get(rs, 1);
	        	T2 p2 = typeHandler2.get(rs, 2);
	            visitor.run(p1,p2);
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
    @SuppressWarnings("unchecked")
    public<T1,T2,T3> void rowsVisitResult(
    		ColumnMeta<T1> c1,
    		ColumnMeta<T2> c2,
    		ColumnMeta<T3> c3,
    		Query filter,
    		Lambda3<T1,T2,T3> visitor) {

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

        	ICustomType<T1> typeHandler1 = (ICustomType<T1>) c1.getTypeHandler();
        	ICustomType<T2> typeHandler2 = (ICustomType<T2>) c2.getTypeHandler();
        	ICustomType<T3> typeHandler3 = (ICustomType<T3>) c3.getTypeHandler();

        	res.executeQuery(q("SELECT ",c1,",",c2,",",c3," FROM ", c1.getMeta()," ", filter));
	        
        	while(res.next()){
	        	ResultSet rs = res.getResultSet();
	        	T1 p1 = typeHandler1.get(rs, 1);
	        	T2 p2 = typeHandler2.get(rs, 2);
	        	T3 p3 = typeHandler3.get(rs, 3);
	            visitor.run(p1,p2,p3);
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
	@SuppressWarnings("unchecked")
	public<T1,T2,T3,T4> void rowsVisitResult(
			ColumnMeta<T1> c1,
			ColumnMeta<T2> c2,
			ColumnMeta<T3> c3,
			ColumnMeta<T4> c4,
			Query filter,
			Lambda4<T1,T2,T3,T4> visitor) {
		
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
			
			ICustomType<T1> typeHandler1 = (ICustomType<T1>) c1.getTypeHandler();
			ICustomType<T2> typeHandler2 = (ICustomType<T2>) c2.getTypeHandler();
			ICustomType<T3> typeHandler3 = (ICustomType<T3>) c3.getTypeHandler();
			ICustomType<T4> typeHandler4 = (ICustomType<T4>) c4.getTypeHandler();
			
			res.executeQuery(q("SELECT ",c1,",",c2,",",c3,",",c4," FROM ", c1.getMeta()," ", filter));
			
			while(res.next()){
				ResultSet rs = res.getResultSet();
				T1 p1 = typeHandler1.get(rs, 1);
				T2 p2 = typeHandler2.get(rs, 2);
				T3 p3 = typeHandler3.get(rs, 3);
				T4 p4 = typeHandler4.get(rs, 4);
				visitor.run(p1,p2,p3,p4);
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
	@SuppressWarnings("unchecked")
	public<T1,T2,T3,T4,T5> void rowsVisitResult(
			ColumnMeta<T1> c1,
			ColumnMeta<T2> c2,
			ColumnMeta<T3> c3,
			ColumnMeta<T4> c4,
			ColumnMeta<T5> c5,
			Query filter,
			Lambda5<T1,T2,T3,T4,T5> visitor) {
		
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
			
			ICustomType<T1> typeHandler1 = (ICustomType<T1>) c1.getTypeHandler();
			ICustomType<T2> typeHandler2 = (ICustomType<T2>) c2.getTypeHandler();
			ICustomType<T3> typeHandler3 = (ICustomType<T3>) c3.getTypeHandler();
			ICustomType<T4> typeHandler4 = (ICustomType<T4>) c4.getTypeHandler();
			ICustomType<T5> typeHandler5 = (ICustomType<T5>) c5.getTypeHandler();
			
			res.executeQuery(q("SELECT ",c1,",",c2,",",c3,",",c4,",",c5," FROM ", c1.getMeta()," ", filter));
			
			while(res.next()){
				ResultSet rs = res.getResultSet();
				T1 p1 = typeHandler1.get(rs, 1);
				T2 p2 = typeHandler2.get(rs, 2);
				T3 p3 = typeHandler3.get(rs, 3);
				T4 p4 = typeHandler4.get(rs, 4);
				T5 p5 = typeHandler5.get(rs, 5);
				visitor.run(p1,p2,p3,p4,p5);
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
    @SuppressWarnings("unchecked")
    public<T1,T2,T3,T4,T5,T6> void rowsVisitResult(
    		ColumnMeta<T1> c1,
    		ColumnMeta<T2> c2,
    		ColumnMeta<T3> c3,
    		ColumnMeta<T4> c4,
    		ColumnMeta<T5> c5,
    		ColumnMeta<T6> c6,
    		Query filter,
    		Lambda6<T1,T2,T3,T4,T5,T6> visitor) {

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

        	ICustomType<T1> typeHandler1 = (ICustomType<T1>) c1.getTypeHandler();
        	ICustomType<T2> typeHandler2 = (ICustomType<T2>) c2.getTypeHandler();
        	ICustomType<T3> typeHandler3 = (ICustomType<T3>) c3.getTypeHandler();
        	ICustomType<T4> typeHandler4 = (ICustomType<T4>) c4.getTypeHandler();
        	ICustomType<T5> typeHandler5 = (ICustomType<T5>) c5.getTypeHandler();
        	ICustomType<T6> typeHandler6 = (ICustomType<T6>) c6.getTypeHandler();

        	res.executeQuery(q("SELECT ",c1,",",c2,",",c3,",",c4,",",c5,",",c6," FROM ", c1.getMeta()," ", filter));
	        
        	while(res.next()){
	        	ResultSet rs = res.getResultSet();
	        	T1 p1 = typeHandler1.get(rs, 1);
	        	T2 p2 = typeHandler2.get(rs, 2);
	        	T3 p3 = typeHandler3.get(rs, 3);
	        	T4 p4 = typeHandler4.get(rs, 4);
	        	T5 p5 = typeHandler5.get(rs, 5);
	        	T6 p6 = typeHandler6.get(rs, 6);
	            visitor.run(p1,p2,p3,p4,p5,p6);
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
	@SuppressWarnings("unchecked")
	public<T1,T2,T3,T4,T5,T6,T7> void rowsVisitResult(
			ColumnMeta<T1> c1,
			ColumnMeta<T2> c2,
			ColumnMeta<T3> c3,
			ColumnMeta<T4> c4,
			ColumnMeta<T5> c5,
			ColumnMeta<T6> c6,
			ColumnMeta<T7> c7,
			Query filter,
			Lambda7<T1,T2,T3,T4,T5,T6,T7> visitor) {
		
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
			
			ICustomType<T1> typeHandler1 = (ICustomType<T1>) c1.getTypeHandler();
			ICustomType<T2> typeHandler2 = (ICustomType<T2>) c2.getTypeHandler();
			ICustomType<T3> typeHandler3 = (ICustomType<T3>) c3.getTypeHandler();
			ICustomType<T4> typeHandler4 = (ICustomType<T4>) c4.getTypeHandler();
			ICustomType<T5> typeHandler5 = (ICustomType<T5>) c5.getTypeHandler();
			ICustomType<T6> typeHandler6 = (ICustomType<T6>) c6.getTypeHandler();
			ICustomType<T7> typeHandler7 = (ICustomType<T7>) c7.getTypeHandler();
			
			res.executeQuery(q("SELECT ",c1,",",c2,",",c3,",",c4,",",c5,",",c6,",",c7," FROM ", c1.getMeta()," ", filter));
			
			while(res.next()){
				ResultSet rs = res.getResultSet();
				T1 p1 = typeHandler1.get(rs, 1);
				T2 p2 = typeHandler2.get(rs, 2);
				T3 p3 = typeHandler3.get(rs, 3);
				T4 p4 = typeHandler4.get(rs, 4);
				T5 p5 = typeHandler5.get(rs, 5);
				T6 p6 = typeHandler6.get(rs, 6);
				T7 p7 = typeHandler7.get(rs, 7);
				visitor.run(p1,p2,p3,p4,p5,p6,p7);
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
	@SuppressWarnings("unchecked")
	public<T1,T2,T3,T4,T5,T6,T7,T8> void rowsVisitResult(
			ColumnMeta<T1> c1,
			ColumnMeta<T2> c2,
			ColumnMeta<T3> c3,
			ColumnMeta<T4> c4,
			ColumnMeta<T5> c5,
			ColumnMeta<T6> c6,
			ColumnMeta<T7> c7,
			ColumnMeta<T8> c8,
			Query filter,
			Lambda8<T1,T2,T3,T4,T5,T6,T7,T8> visitor) {
		
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
			
			ICustomType<T1> typeHandler1 = (ICustomType<T1>) c1.getTypeHandler();
			ICustomType<T2> typeHandler2 = (ICustomType<T2>) c2.getTypeHandler();
			ICustomType<T3> typeHandler3 = (ICustomType<T3>) c3.getTypeHandler();
			ICustomType<T4> typeHandler4 = (ICustomType<T4>) c4.getTypeHandler();
			ICustomType<T5> typeHandler5 = (ICustomType<T5>) c5.getTypeHandler();
			ICustomType<T6> typeHandler6 = (ICustomType<T6>) c6.getTypeHandler();
			ICustomType<T7> typeHandler7 = (ICustomType<T7>) c7.getTypeHandler();
			ICustomType<T8> typeHandler8 = (ICustomType<T8>) c8.getTypeHandler();
			
			res.executeQuery(q("SELECT ",c1,",",c2,",",c3,",",c4,",",c5,",",c6,",",c7,",",c8," FROM ", c1.getMeta()," ", filter));
			
			while(res.next()){
				ResultSet rs = res.getResultSet();
				T1 p1 = typeHandler1.get(rs, 1);
				T2 p2 = typeHandler2.get(rs, 2);
				T3 p3 = typeHandler3.get(rs, 3);
				T4 p4 = typeHandler4.get(rs, 4);
				T5 p5 = typeHandler5.get(rs, 5);
				T6 p6 = typeHandler6.get(rs, 6);
				T7 p7 = typeHandler7.get(rs, 7);
				T8 p8 = typeHandler8.get(rs, 8);
				visitor.run(p1,p2,p3,p4,p5,p6,p7,p8);
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
    @SuppressWarnings("unchecked")
    public<T1,T2,T3,T4,T5,T6,T7,T8,T9> void rowsVisitResult(
    		ColumnMeta<T1> c1,
    		ColumnMeta<T2> c2,
    		ColumnMeta<T3> c3,
    		ColumnMeta<T4> c4,
    		ColumnMeta<T5> c5,
    		ColumnMeta<T6> c6,
    		ColumnMeta<T7> c7,
    		ColumnMeta<T8> c8,
    		ColumnMeta<T9> c9,
    		Query filter,
    		Lambda9<T1,T2,T3,T4,T5,T6,T7,T8,T9> visitor) {

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

        	ICustomType<T1> typeHandler1 = (ICustomType<T1>) c1.getTypeHandler();
        	ICustomType<T2> typeHandler2 = (ICustomType<T2>) c2.getTypeHandler();
        	ICustomType<T3> typeHandler3 = (ICustomType<T3>) c3.getTypeHandler();
        	ICustomType<T4> typeHandler4 = (ICustomType<T4>) c4.getTypeHandler();
        	ICustomType<T5> typeHandler5 = (ICustomType<T5>) c5.getTypeHandler();
        	ICustomType<T6> typeHandler6 = (ICustomType<T6>) c6.getTypeHandler();
        	ICustomType<T7> typeHandler7 = (ICustomType<T7>) c7.getTypeHandler();
        	ICustomType<T8> typeHandler8 = (ICustomType<T8>) c8.getTypeHandler();
        	ICustomType<T9> typeHandler9 = (ICustomType<T9>) c9.getTypeHandler();

        	res.executeQuery(q("SELECT ",c1,",",c2,",",c3,",",c4,",",c5,",",c6,",",c7,",",c8,",",c9," FROM ", c1.getMeta()," ", filter));
	        
        	while(res.next()){
	        	ResultSet rs = res.getResultSet();
	        	T1 p1 = typeHandler1.get(rs, 1);
	        	T2 p2 = typeHandler2.get(rs, 2);
	        	T3 p3 = typeHandler3.get(rs, 3);
	        	T4 p4 = typeHandler4.get(rs, 4);
	        	T5 p5 = typeHandler5.get(rs, 5);
	        	T6 p6 = typeHandler6.get(rs, 6);
	        	T7 p7 = typeHandler7.get(rs, 7);
	        	T8 p8 = typeHandler8.get(rs, 8);
	        	T9 p9 = typeHandler9.get(rs, 9);
	            visitor.run(p1,p2,p3,p4,p5,p6,p7,p8,p9);
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
	@SuppressWarnings("unchecked")
	public<T1,T2,T3,T4,T5,T6,T7,T8,T9,T10> void rowsVisitResult(
			ColumnMeta<T1> c1,
			ColumnMeta<T2> c2,
			ColumnMeta<T3> c3,
			ColumnMeta<T4> c4,
			ColumnMeta<T5> c5,
			ColumnMeta<T6> c6,
			ColumnMeta<T7> c7,
			ColumnMeta<T8> c8,
			ColumnMeta<T9> c9,
			ColumnMeta<T10> c10,
			Query filter,
			Lambda10<T1,T2,T3,T4,T5,T6,T7,T8,T9,T10> visitor) {
		
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
			
			ICustomType<T1> typeHandler1 = (ICustomType<T1>) c1.getTypeHandler();
			ICustomType<T2> typeHandler2 = (ICustomType<T2>) c2.getTypeHandler();
			ICustomType<T3> typeHandler3 = (ICustomType<T3>) c3.getTypeHandler();
			ICustomType<T4> typeHandler4 = (ICustomType<T4>) c4.getTypeHandler();
			ICustomType<T5> typeHandler5 = (ICustomType<T5>) c5.getTypeHandler();
			ICustomType<T6> typeHandler6 = (ICustomType<T6>) c6.getTypeHandler();
			ICustomType<T7> typeHandler7 = (ICustomType<T7>) c7.getTypeHandler();
			ICustomType<T8> typeHandler8 = (ICustomType<T8>) c8.getTypeHandler();
			ICustomType<T9> typeHandler9 = (ICustomType<T9>) c9.getTypeHandler();
			ICustomType<T10> typeHandler10 = (ICustomType<T10>) c10.getTypeHandler();
			
			res.executeQuery(q("SELECT ",c1,",",c2,",",c3,",",c4,",",c5,",",c6,",",c7,",",c8,",",c9,",",c10," FROM ", c1.getMeta()," ", filter));
			
			while(res.next()){
				ResultSet rs = res.getResultSet();
				T1 p1 = typeHandler1.get(rs, 1);
				T2 p2 = typeHandler2.get(rs, 2);
				T3 p3 = typeHandler3.get(rs, 3);
				T4 p4 = typeHandler4.get(rs, 4);
				T5 p5 = typeHandler5.get(rs, 5);
				T6 p6 = typeHandler6.get(rs, 6);
				T7 p7 = typeHandler7.get(rs, 7);
				T8 p8 = typeHandler8.get(rs, 8);
				T9 p9 = typeHandler9.get(rs, 9);
				T10 p10 = typeHandler10.get(rs, 10);
				visitor.run(p1,p2,p3,p4,p5,p6,p7,p8,p9,p10);
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
	@SuppressWarnings("unchecked")
	public<T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11> void rowsVisitResult(
			ColumnMeta<T1> c1,
			ColumnMeta<T2> c2,
			ColumnMeta<T3> c3,
			ColumnMeta<T4> c4,
			ColumnMeta<T5> c5,
			ColumnMeta<T6> c6,
			ColumnMeta<T7> c7,
			ColumnMeta<T8> c8,
			ColumnMeta<T9> c9,
			ColumnMeta<T10> c10,
			ColumnMeta<T11> c11,
			Query filter,
			Lambda11<T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11> visitor) {
		
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
			
			ICustomType<T1> typeHandler1 = (ICustomType<T1>) c1.getTypeHandler();
			ICustomType<T2> typeHandler2 = (ICustomType<T2>) c2.getTypeHandler();
			ICustomType<T3> typeHandler3 = (ICustomType<T3>) c3.getTypeHandler();
			ICustomType<T4> typeHandler4 = (ICustomType<T4>) c4.getTypeHandler();
			ICustomType<T5> typeHandler5 = (ICustomType<T5>) c5.getTypeHandler();
			ICustomType<T6> typeHandler6 = (ICustomType<T6>) c6.getTypeHandler();
			ICustomType<T7> typeHandler7 = (ICustomType<T7>) c7.getTypeHandler();
			ICustomType<T8> typeHandler8 = (ICustomType<T8>) c8.getTypeHandler();
			ICustomType<T9> typeHandler9 = (ICustomType<T9>) c9.getTypeHandler();
			ICustomType<T10> typeHandler10 = (ICustomType<T10>) c10.getTypeHandler();
			ICustomType<T11> typeHandler11 = (ICustomType<T11>) c11.getTypeHandler();
			
			res.executeQuery(q("SELECT ",c1,",",c2,",",c3,",",c4,",",c5,",",c6,",",c7,",",c8,",",c9,",",c10,",",c11," FROM ", c1.getMeta()," ", filter));
			
			while(res.next()){
				ResultSet rs = res.getResultSet();
				T1 p1 = typeHandler1.get(rs, 1);
				T2 p2 = typeHandler2.get(rs, 2);
				T3 p3 = typeHandler3.get(rs, 3);
				T4 p4 = typeHandler4.get(rs, 4);
				T5 p5 = typeHandler5.get(rs, 5);
				T6 p6 = typeHandler6.get(rs, 6);
				T7 p7 = typeHandler7.get(rs, 7);
				T8 p8 = typeHandler8.get(rs, 8);
				T9 p9 = typeHandler9.get(rs, 9);
				T10 p10 = typeHandler10.get(rs, 10);
				T11 p11 = typeHandler11.get(rs, 11);
				visitor.run(p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11);
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
	@SuppressWarnings("unchecked")
	public<T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11, T12> void rowsVisitResult(
			ColumnMeta<T1> c1,
			ColumnMeta<T2> c2,
			ColumnMeta<T3> c3,
			ColumnMeta<T4> c4,
			ColumnMeta<T5> c5,
			ColumnMeta<T6> c6,
			ColumnMeta<T7> c7,
			ColumnMeta<T8> c8,
			ColumnMeta<T9> c9,
			ColumnMeta<T10> c10,
			ColumnMeta<T11> c11,
			ColumnMeta<T12> c12,
			Query filter,
			Lambda12<T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11, T12> visitor) {
		
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
			
			ICustomType<T1> typeHandler1 = (ICustomType<T1>) c1.getTypeHandler();
			ICustomType<T2> typeHandler2 = (ICustomType<T2>) c2.getTypeHandler();
			ICustomType<T3> typeHandler3 = (ICustomType<T3>) c3.getTypeHandler();
			ICustomType<T4> typeHandler4 = (ICustomType<T4>) c4.getTypeHandler();
			ICustomType<T5> typeHandler5 = (ICustomType<T5>) c5.getTypeHandler();
			ICustomType<T6> typeHandler6 = (ICustomType<T6>) c6.getTypeHandler();
			ICustomType<T7> typeHandler7 = (ICustomType<T7>) c7.getTypeHandler();
			ICustomType<T8> typeHandler8 = (ICustomType<T8>) c8.getTypeHandler();
			ICustomType<T9> typeHandler9 = (ICustomType<T9>) c9.getTypeHandler();
			ICustomType<T10> typeHandler10 = (ICustomType<T10>) c10.getTypeHandler();
			ICustomType<T11> typeHandler11 = (ICustomType<T11>) c11.getTypeHandler();
			ICustomType<T12> typeHandler12 = (ICustomType<T12>) c12.getTypeHandler();
			
			res.executeQuery(q("SELECT ",c1,",",c2,",",c3,",",c4,",",c5,",",c6,",",c7,",",c8,",",c9,",",c10,",",c11,",",c12," FROM ", c1.getMeta()," ", filter));
			
			while(res.next()){
				ResultSet rs = res.getResultSet();
				T1 p1 = typeHandler1.get(rs, 1);
				T2 p2 = typeHandler2.get(rs, 2);
				T3 p3 = typeHandler3.get(rs, 3);
				T4 p4 = typeHandler4.get(rs, 4);
				T5 p5 = typeHandler5.get(rs, 5);
				T6 p6 = typeHandler6.get(rs, 6);
				T7 p7 = typeHandler7.get(rs, 7);
				T8 p8 = typeHandler8.get(rs, 8);
				T9 p9 = typeHandler9.get(rs, 9);
				T10 p10 = typeHandler10.get(rs, 10);
				T11 p11 = typeHandler11.get(rs, 11);
				T12 p12 = typeHandler12.get(rs, 12);
				visitor.run(p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12);
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
	@SuppressWarnings("unchecked")
	public<T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11, T12, T13> void rowsVisitResult(
			ColumnMeta<T1> c1,
			ColumnMeta<T2> c2,
			ColumnMeta<T3> c3,
			ColumnMeta<T4> c4,
			ColumnMeta<T5> c5,
			ColumnMeta<T6> c6,
			ColumnMeta<T7> c7,
			ColumnMeta<T8> c8,
			ColumnMeta<T9> c9,
			ColumnMeta<T10> c10,
			ColumnMeta<T11> c11,
			ColumnMeta<T12> c12,
			ColumnMeta<T13> c13,
			Query filter,
			Lambda13<T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11, T12,T13> visitor) {
		
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
			
			ICustomType<T1> typeHandler1 = (ICustomType<T1>) c1.getTypeHandler();
			ICustomType<T2> typeHandler2 = (ICustomType<T2>) c2.getTypeHandler();
			ICustomType<T3> typeHandler3 = (ICustomType<T3>) c3.getTypeHandler();
			ICustomType<T4> typeHandler4 = (ICustomType<T4>) c4.getTypeHandler();
			ICustomType<T5> typeHandler5 = (ICustomType<T5>) c5.getTypeHandler();
			ICustomType<T6> typeHandler6 = (ICustomType<T6>) c6.getTypeHandler();
			ICustomType<T7> typeHandler7 = (ICustomType<T7>) c7.getTypeHandler();
			ICustomType<T8> typeHandler8 = (ICustomType<T8>) c8.getTypeHandler();
			ICustomType<T9> typeHandler9 = (ICustomType<T9>) c9.getTypeHandler();
			ICustomType<T10> typeHandler10 = (ICustomType<T10>) c10.getTypeHandler();
			ICustomType<T11> typeHandler11 = (ICustomType<T11>) c11.getTypeHandler();
			ICustomType<T12> typeHandler12 = (ICustomType<T12>) c12.getTypeHandler();
			ICustomType<T13> typeHandler13 = (ICustomType<T13>) c13.getTypeHandler();
			
			res.executeQuery(q("SELECT ",c1,",",c2,",",c3,",",c4,",",c5,",",c6,",",c7,",",c8,",",c9,",",c10,",",c11,",",c12,",",c13," FROM ", c1.getMeta()," ", filter));
			
			while(res.next()){
				ResultSet rs = res.getResultSet();
				T1 p1 = typeHandler1.get(rs, 1);
				T2 p2 = typeHandler2.get(rs, 2);
				T3 p3 = typeHandler3.get(rs, 3);
				T4 p4 = typeHandler4.get(rs, 4);
				T5 p5 = typeHandler5.get(rs, 5);
				T6 p6 = typeHandler6.get(rs, 6);
				T7 p7 = typeHandler7.get(rs, 7);
				T8 p8 = typeHandler8.get(rs, 8);
				T9 p9 = typeHandler9.get(rs, 9);
				T10 p10 = typeHandler10.get(rs, 10);
				T11 p11 = typeHandler11.get(rs, 11);
				T12 p12 = typeHandler12.get(rs, 12);
				T13 p13 = typeHandler13.get(rs, 13);
				visitor.run(p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13);
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
	@SuppressWarnings("unchecked")
	public<T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11, T12, T13, T14> void rowsVisitResult(
			ColumnMeta<T1> c1,
			ColumnMeta<T2> c2,
			ColumnMeta<T3> c3,
			ColumnMeta<T4> c4,
			ColumnMeta<T5> c5,
			ColumnMeta<T6> c6,
			ColumnMeta<T7> c7,
			ColumnMeta<T8> c8,
			ColumnMeta<T9> c9,
			ColumnMeta<T10> c10,
			ColumnMeta<T11> c11,
			ColumnMeta<T12> c12,
			ColumnMeta<T13> c13,
			ColumnMeta<T14> c14,
			Query filter,
			Lambda14<T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11, T12,T13,T14> visitor) {
		
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
			
			ICustomType<T1> typeHandler1 = (ICustomType<T1>) c1.getTypeHandler();
			ICustomType<T2> typeHandler2 = (ICustomType<T2>) c2.getTypeHandler();
			ICustomType<T3> typeHandler3 = (ICustomType<T3>) c3.getTypeHandler();
			ICustomType<T4> typeHandler4 = (ICustomType<T4>) c4.getTypeHandler();
			ICustomType<T5> typeHandler5 = (ICustomType<T5>) c5.getTypeHandler();
			ICustomType<T6> typeHandler6 = (ICustomType<T6>) c6.getTypeHandler();
			ICustomType<T7> typeHandler7 = (ICustomType<T7>) c7.getTypeHandler();
			ICustomType<T8> typeHandler8 = (ICustomType<T8>) c8.getTypeHandler();
			ICustomType<T9> typeHandler9 = (ICustomType<T9>) c9.getTypeHandler();
			ICustomType<T10> typeHandler10 = (ICustomType<T10>) c10.getTypeHandler();
			ICustomType<T11> typeHandler11 = (ICustomType<T11>) c11.getTypeHandler();
			ICustomType<T12> typeHandler12 = (ICustomType<T12>) c12.getTypeHandler();
			ICustomType<T13> typeHandler13 = (ICustomType<T13>) c13.getTypeHandler();
			ICustomType<T14> typeHandler14 = (ICustomType<T14>) c14.getTypeHandler();
			
			res.executeQuery(q("SELECT ",c1,",",c2,",",c3,",",c4,",",c5,",",c6,",",c7,",",c8,",",c9,",",c10,",",c11,",",c12,",",c13,",",c14," FROM ", c1.getMeta()," ", filter));
			
			while(res.next()){
				ResultSet rs = res.getResultSet();
				T1 p1 = typeHandler1.get(rs, 1);
				T2 p2 = typeHandler2.get(rs, 2);
				T3 p3 = typeHandler3.get(rs, 3);
				T4 p4 = typeHandler4.get(rs, 4);
				T5 p5 = typeHandler5.get(rs, 5);
				T6 p6 = typeHandler6.get(rs, 6);
				T7 p7 = typeHandler7.get(rs, 7);
				T8 p8 = typeHandler8.get(rs, 8);
				T9 p9 = typeHandler9.get(rs, 9);
				T10 p10 = typeHandler10.get(rs, 10);
				T11 p11 = typeHandler11.get(rs, 11);
				T12 p12 = typeHandler12.get(rs, 12);
				T13 p13 = typeHandler13.get(rs, 13);
				T14 p14 = typeHandler14.get(rs, 14);
				visitor.run(p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,p14);
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
	@SuppressWarnings("unchecked")
	public<T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11, T12, T13, T14, T15> void rowsVisitResult(
			ColumnMeta<T1> c1,
			ColumnMeta<T2> c2,
			ColumnMeta<T3> c3,
			ColumnMeta<T4> c4,
			ColumnMeta<T5> c5,
			ColumnMeta<T6> c6,
			ColumnMeta<T7> c7,
			ColumnMeta<T8> c8,
			ColumnMeta<T9> c9,
			ColumnMeta<T10> c10,
			ColumnMeta<T11> c11,
			ColumnMeta<T12> c12,
			ColumnMeta<T13> c13,
			ColumnMeta<T14> c14,
			ColumnMeta<T15> c15,
			Query filter,
			Lambda15<T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11, T12,T13,T14,T15> visitor) {
		
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
			
			ICustomType<T1> typeHandler1 = (ICustomType<T1>) c1.getTypeHandler();
			ICustomType<T2> typeHandler2 = (ICustomType<T2>) c2.getTypeHandler();
			ICustomType<T3> typeHandler3 = (ICustomType<T3>) c3.getTypeHandler();
			ICustomType<T4> typeHandler4 = (ICustomType<T4>) c4.getTypeHandler();
			ICustomType<T5> typeHandler5 = (ICustomType<T5>) c5.getTypeHandler();
			ICustomType<T6> typeHandler6 = (ICustomType<T6>) c6.getTypeHandler();
			ICustomType<T7> typeHandler7 = (ICustomType<T7>) c7.getTypeHandler();
			ICustomType<T8> typeHandler8 = (ICustomType<T8>) c8.getTypeHandler();
			ICustomType<T9> typeHandler9 = (ICustomType<T9>) c9.getTypeHandler();
			ICustomType<T10> typeHandler10 = (ICustomType<T10>) c10.getTypeHandler();
			ICustomType<T11> typeHandler11 = (ICustomType<T11>) c11.getTypeHandler();
			ICustomType<T12> typeHandler12 = (ICustomType<T12>) c12.getTypeHandler();
			ICustomType<T13> typeHandler13 = (ICustomType<T13>) c13.getTypeHandler();
			ICustomType<T14> typeHandler14 = (ICustomType<T14>) c14.getTypeHandler();
			ICustomType<T15> typeHandler15 = (ICustomType<T15>) c15.getTypeHandler();
			
			res.executeQuery(q("SELECT ",c1,",",c2,",",c3,",",c4,",",c5,",",c6,",",c7,",",c8,",",c9,",",c10,",",c11,",",c12,",",c13,",",c14,",",c15," FROM ", c1.getMeta()," ", filter));
			
			while(res.next()){
				ResultSet rs = res.getResultSet();
				T1 p1 = typeHandler1.get(rs, 1);
				T2 p2 = typeHandler2.get(rs, 2);
				T3 p3 = typeHandler3.get(rs, 3);
				T4 p4 = typeHandler4.get(rs, 4);
				T5 p5 = typeHandler5.get(rs, 5);
				T6 p6 = typeHandler6.get(rs, 6);
				T7 p7 = typeHandler7.get(rs, 7);
				T8 p8 = typeHandler8.get(rs, 8);
				T9 p9 = typeHandler9.get(rs, 9);
				T10 p10 = typeHandler10.get(rs, 10);
				T11 p11 = typeHandler11.get(rs, 11);
				T12 p12 = typeHandler12.get(rs, 12);
				T13 p13 = typeHandler13.get(rs, 13);
				T14 p14 = typeHandler14.get(rs, 14);
				T15 p15 = typeHandler15.get(rs, 15);
				visitor.run(p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,p14,p15);
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
	@SuppressWarnings("unchecked")
	public<T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11, T12, T13, T14, T15,T16> void rowsVisitResult(
			ColumnMeta<T1> c1,
			ColumnMeta<T2> c2,
			ColumnMeta<T3> c3,
			ColumnMeta<T4> c4,
			ColumnMeta<T5> c5,
			ColumnMeta<T6> c6,
			ColumnMeta<T7> c7,
			ColumnMeta<T8> c8,
			ColumnMeta<T9> c9,
			ColumnMeta<T10> c10,
			ColumnMeta<T11> c11,
			ColumnMeta<T12> c12,
			ColumnMeta<T13> c13,
			ColumnMeta<T14> c14,
			ColumnMeta<T15> c15,
			ColumnMeta<T16> c16,
			Query filter,
			Lambda16<T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11, T12,T13,T14,T15,T16> visitor) {
		
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
			
			ICustomType<T1> typeHandler1 = (ICustomType<T1>) c1.getTypeHandler();
			ICustomType<T2> typeHandler2 = (ICustomType<T2>) c2.getTypeHandler();
			ICustomType<T3> typeHandler3 = (ICustomType<T3>) c3.getTypeHandler();
			ICustomType<T4> typeHandler4 = (ICustomType<T4>) c4.getTypeHandler();
			ICustomType<T5> typeHandler5 = (ICustomType<T5>) c5.getTypeHandler();
			ICustomType<T6> typeHandler6 = (ICustomType<T6>) c6.getTypeHandler();
			ICustomType<T7> typeHandler7 = (ICustomType<T7>) c7.getTypeHandler();
			ICustomType<T8> typeHandler8 = (ICustomType<T8>) c8.getTypeHandler();
			ICustomType<T9> typeHandler9 = (ICustomType<T9>) c9.getTypeHandler();
			ICustomType<T10> typeHandler10 = (ICustomType<T10>) c10.getTypeHandler();
			ICustomType<T11> typeHandler11 = (ICustomType<T11>) c11.getTypeHandler();
			ICustomType<T12> typeHandler12 = (ICustomType<T12>) c12.getTypeHandler();
			ICustomType<T13> typeHandler13 = (ICustomType<T13>) c13.getTypeHandler();
			ICustomType<T14> typeHandler14 = (ICustomType<T14>) c14.getTypeHandler();
			ICustomType<T15> typeHandler15 = (ICustomType<T15>) c15.getTypeHandler();
			ICustomType<T16> typeHandler16 = (ICustomType<T16>) c16.getTypeHandler();
			
			res.executeQuery(q("SELECT ",c1,",",c2,",",c3,",",c4,",",c5,",",c6,",",c7,",",c8,",",c9,",",c10,",",c11,",",c12,",",c13,",",c14,",",c15,",",c16," FROM ", c1.getMeta()," ", filter));
			
			while(res.next()){
				ResultSet rs = res.getResultSet();
				T1 p1 = typeHandler1.get(rs, 1);
				T2 p2 = typeHandler2.get(rs, 2);
				T3 p3 = typeHandler3.get(rs, 3);
				T4 p4 = typeHandler4.get(rs, 4);
				T5 p5 = typeHandler5.get(rs, 5);
				T6 p6 = typeHandler6.get(rs, 6);
				T7 p7 = typeHandler7.get(rs, 7);
				T8 p8 = typeHandler8.get(rs, 8);
				T9 p9 = typeHandler9.get(rs, 9);
				T10 p10 = typeHandler10.get(rs, 10);
				T11 p11 = typeHandler11.get(rs, 11);
				T12 p12 = typeHandler12.get(rs, 12);
				T13 p13 = typeHandler13.get(rs, 13);
				T14 p14 = typeHandler14.get(rs, 14);
				T15 p15 = typeHandler15.get(rs, 15);
				T16 p16 = typeHandler16.get(rs, 16);
				visitor.run(p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,p14,p15,p16);
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
}
