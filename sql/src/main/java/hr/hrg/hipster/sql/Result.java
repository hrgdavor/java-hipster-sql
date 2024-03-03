package hr.hrg.hipster.sql;

import java.sql.*;
import java.util.*;

import hr.hrg.hipster.entity.*;
import hr.hrg.hipster.query.*;
/**
 * 
 * @author hrg
 */
public class Result implements AutoCloseable{
	
	private final HipsterConnectionImpl hipConnection;
	private final HipsterSql hipster;
    protected ResultSet rs;
    protected PreparedStatement ps;
    private ResultSetMetaData metaData;
	private int fetchSize;
	private IQueryValue[] values;
	protected String query;
    protected Throwable trace;
    protected long startTime;

    public Result(IHipsterConnection conn){
    	this.hipConnection = (HipsterConnectionImpl) conn;
    	this.hipster = conn.getHipster();
    }

    void logQuery() {
    	if(trace == null) return;
    	
    	IQueryLogger queryLogger = hipster.getQueryLogger();
		if(queryLogger == null || !queryLogger.isLogging()) return;
		
		List params = new ArrayList<>();
		if(values != null) for(IQueryValue value:values) {
			params.add(value == null ? null : value.getValue());
		}
		queryLogger.logQuery(query, trace, startTime, System.currentTimeMillis() - startTime, params);
    }
    
    /** Execute the provided query
     * @param queryParts query parts 
     * @return self (builder pattern)
     */
    public Result executeQuery(Object ...queryParts){
    	prepareQuery(false, queryParts);
    	return execute();
    }

    public Result executePrepared(String query, Object ...parts){
    	return executeQuery(hipster.q(parts));
    }

    private void prepareQuery(boolean returnGeneratedKeys, Object ...queryParts){
    	Query query = null;
    	
    	if(queryParts.length == 1 && queryParts[0] instanceof Query){
    		query = (Query) queryParts[0];
    	}else
    		query = hipster.q(queryParts);

    	this.hipConnection.lastQuery = query;// will be null if executing a PreparedQuery
    
		prepareForExecution(query, returnGeneratedKeys);
    }

	private void prepareForExecution(Query p, boolean returnGeneratedKeys){

    	try {
        	IQueryLogger queryLogger = hipster.getQueryLogger();
    		if(queryLogger != null && queryLogger.isLogging()) {
    			startTime = System.currentTimeMillis();
    			trace = new Throwable("query dump");
    		}
			query = p.getQueryExpression().toString();
			if(returnGeneratedKeys)
				ps = hipConnection.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			else
				ps = hipConnection.getConnection().prepareStatement(query);
			ps.setFetchSize(this.fetchSize);
		} catch (SQLException e) {
			throw new RuntimeException("Error preparing statement: "+hipConnection.lastQuery+" ERR: "+e.getMessage(), e);
		}
		
		this.values = p.getValues();
		int size = p.getSize();
		for(int i=0; i<size; i++){
			try {
				values[i].set(ps, i+1);
			} catch (SQLException e) {
				throw new RuntimeException("Error filling prepared statement: "+this.hipConnection.lastQuery+" on index "+(i+1)+" using value "+values[i]+" ERR: "+e.getMessage(), e);
			}
		}    	
    }

	private Result execute() {
		try{
            boolean hasResults = ps.execute();
            if(hasResults) {
            	rs = ps.getResultSet();
            	metaData = rs.getMetaData();
            }
            return this;
        }catch (Exception e){
            close(); throw queryError(e);
        }
	}

	Result executeUpdate(Object ...queryParts){
    	prepareQuery(true, queryParts);
    	try{
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            metaData = rs.getMetaData();
            return this;
        }catch (Exception e){
            close(); throw queryError(e);
        }
	}
	
    private RuntimeException queryError(Exception e) {
        return new HipsterSqlException(hipConnection, "Problem executing query "+e.getMessage(), e);
    }

    public int update(Object ...queryParts){
        try{
        	prepareQuery(true, queryParts);
        	return ps.executeUpdate();
        }catch (Exception e){
            close();
            throw queryError(e);
        }
    }
    
    public Long fetchLong(){
    	try{
    		if(rs == null || !rs.next()) return null;
    		return rs.getLong(1);
    	}catch (Exception e){
    		close(); throw queryError(e);
    	}
    }
    
    public Double fetchDouble(){
    	try{
    		if(rs == null || !rs.next()) return null;
    		return rs.getDouble(1);
    	}catch (Exception e){
    		close(); throw queryError(e);
    	}
    }
    
    public Integer fetchInteger(){
    	try{
    		if(rs == null || !rs.next()) return null;
    		return rs.getInt(1);
    	}catch (Exception e){
    		close(); throw queryError(e);
    	}
    }
    
    public Float fetchFloat(){
    	try{
    		if(rs == null || !rs.next()) return null;
    		return rs.getFloat(1);
    	}catch (Exception e){
    		close(); throw queryError(e);
    	}
    }
    
    public String fetchString(){
        try{
            if(rs == null || !rs.next()) return null;
            return rs.getString(1);
        }catch (Exception e){
            close(); throw queryError(e);
        }
    }

    /**
     * 
     * @return list of values returned by the current {@link ResultSet}
     */
    public List<Object> fetchRow(){
        try{
            if(rs == null || !rs.next()) return null;
            int count = metaData.getColumnCount();
            List<Object> row = new ArrayList<Object>();
            for(int i=1; i<=count; i++){
                row.add(hipster.handleRsGet(hipConnection, rs,i,metaData.getColumnType(i), metaData.getColumnName(i)));
            }
            return row;
        }catch (Exception e){
            close(); throw queryError(e);
        }
    }

    /** @return null (if no more rows) or map of columnName:value.*/
    public Map<Object,Object> fetchAssoc(){
        try{
            if(rs == null || !rs.next()) return null;
            int count = metaData.getColumnCount();
            Map<Object, Object> row = new LinkedHashMap<>();
            for(int i=1; i<=count; i++){
                String columnName = metaData.getColumnName(i);
                row.put(columnName, hipster.handleRsGet(hipConnection, rs,i,metaData.getColumnType(i), columnName));
            }
            return row;
        }catch (Exception e){
            close(); throw queryError(e);
        }
    }
    
    public <T,ID> T fetchEntity(IEntityMeta<T,ID> reader){
        try{
            if(rs == null || !rs.next()) return null;
            return reader.fromResultSet(rs);
        }catch (Exception e){
            close(); throw queryError(e);
        }
    }


    public void close() {
		try {
			logQuery();
		} catch (Exception e) {
			HipsterSql.log.error("Error logging query " + this.hipConnection.lastQuery + " ERR:"+ e.getMessage(), e);
		}
    		
		if (ps != null) {
			try {
				ps.close();
			} catch (Exception e) {
				HipsterSql.log.error("Error closing prepared statement " + this.hipConnection.lastQuery + " ERR:"+ e.getMessage(), e);
			}
			ps = null;
		}
		if (rs != null) {
			try {
				rs.close();
			} catch (Exception e) {
				HipsterSql.log.error("Error closing resultset ERR:" + e.getMessage(), e);
			}
			rs = null;
		}
    }
    
    public ResultSet getResultSet() {
		return rs;
	}
    
    /** Call ResultSet.next() but throw RuntimeException to avoid polluting calling code with try/catch
     * 
     * @return if next is available
     */
	public boolean next() {
		try {
			return rs ==null ? false:rs.next();
		} catch (SQLException e) {
			throw new HipsterSqlException(hipConnection, "error getting next result", e);
		}
	}
	
	public int getFetchSize() {
		return fetchSize;
	}
	
	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}
}