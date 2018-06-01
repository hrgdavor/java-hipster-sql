package hr.hrg.hipster.sql;

import java.sql.*;
import java.util.*;
/**
 * 
 * @author hrg
 */
public class Result implements AutoCloseable{
	
	private final HipsterConnectionImpl hipConnection;
	private final HipsterSql hipster;
	protected String query;
    protected ResultSet rs;
    protected PreparedStatement ps;
    private ResultSetMetaData metaData;
	private int fetchSize;

    public Result(IHipsterConnection conn){
    	this.hipConnection = (HipsterConnectionImpl) conn;
    	this.hipster = conn.getHipster();
    }

    /** Execute the provided query
     * 
     * @return self (builder pattern)
     */
    public Result executeQuery(Object ...queryParts){
    	prepareQuery(false, queryParts);
    	return execute();
    }

    public Result executePrepared(String query, Object ...params){
    	return executeQuery(new PreparedQuery(hipster.getTypeSource(),query, params));
    }

    private void prepareQuery(boolean returnGeneratedKeys, Object ...queryParts){
    	Query query = null;
    	PreparedQuery prepared = null;
    	
    	if(queryParts.length == 1 && queryParts[0] instanceof Query){
    		query = (Query) queryParts[0];
    	}else if(queryParts.length == 1 && queryParts[0] instanceof PreparedQuery){
    		prepared = (PreparedQuery) queryParts[0];
    	}else
    		query = new Query(queryParts);

    	this.hipConnection.lastQuery = query;// will be null if executing a PreparedQuery
    
		if(prepared == null) prepared = hipster.prepare(query);
		
		prepareForExecution(prepared, returnGeneratedKeys);
    }

    private void prepareForExecution(PreparedQuery p, boolean returnGeneratedKeys){
        this.hipConnection.lastPrepared = p;

		try {
			if(returnGeneratedKeys)
				ps = hipConnection.getConnection().prepareStatement(p.getQueryString(), Statement.RETURN_GENERATED_KEYS);
			else
				ps = hipConnection.getConnection().prepareStatement(p.getQueryString());
			ps.setFetchSize(this.fetchSize);
		} catch (SQLException e) {
			throw new RuntimeException("Error preparing statement: "+this.hipConnection.lastPrepared.getQueryString()+" ERR: "+e.getMessage(), e);
		}

		List<Object> params = p.getParams();
		for(int i=0; i<params.size(); i++){
			try {
				hipster.prepSet(hipConnection,ps,i+1,params.get(i));
			} catch (SQLException e) {
				throw new RuntimeException("Error filling prepared statement: "+this.hipConnection.lastPrepared.getQueryString()+" on index "+(i+1)+" using value "+params.get(i)+" ERR: "+e.getMessage(), e);
			}
		}    	
    }

	private Result execute() {
		try{
            rs = ps.executeQuery();
            metaData = rs.getMetaData();
            return this;
        }catch (Exception e){
            close(); throw queryError(e);
        }
	}

	public Result executeUpdate(Object ...queryParts){
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
    		if(!rs.next()) return null;
    		return rs.getLong(1);
    	}catch (Exception e){
    		close(); throw queryError(e);
    	}
    }
    
    public Double fetchDouble(){
    	try{
    		if(!rs.next()) return null;
    		return rs.getDouble(1);
    	}catch (Exception e){
    		close(); throw queryError(e);
    	}
    }
    
    public Integer fetchInteger(){
    	try{
    		if(!rs.next()) return null;
    		return rs.getInt(1);
    	}catch (Exception e){
    		close(); throw queryError(e);
    	}
    }
    
    public Float fetchFloat(){
    	try{
    		if(!rs.next()) return null;
    		return rs.getFloat(1);
    	}catch (Exception e){
    		close(); throw queryError(e);
    	}
    }
    
    public String fetchString(){
        try{
            if(!rs.next()) return null;
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
            if(!rs.next()) return null;
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
            if(!rs.next()) return null;
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
    
    public <T,E extends BaseColumnMeta> T fetchEntity(IReadMeta<T, E> reader){
        try{
            if(!rs.next()) return null;
            return reader.fromResultSet(rs);
        }catch (Exception e){
            close(); throw queryError(e);
        }
    }


    public void close() {
		if (ps != null) {
			try {
				ps.close();
			} catch (Exception e) {
				HipsterSql.log.error("Error closing prepared statement " + this.hipConnection.lastPrepared + " ERR:"+ e.getMessage(), e);
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
    
    /** Call ResultSet.next() but throw RuntimeException to avoid polluting calling code with try/catch*/
	public boolean next() {
		try {
			return rs.next();
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