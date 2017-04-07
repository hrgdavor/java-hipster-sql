package hr.hrg.hipstersql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/**
 * 
 * @author hrg
 */
class Result implements AutoCloseable{

	private final HipsterConnection hipConnection;
	private final HipsterSql hipster;
	protected String query;
    protected ResultSet rs;
    protected PreparedStatement ps;
    private ResultSetMetaData metaData;

    public Result(HipsterConnection hipConnection, Object ... queryParts ){
    	this.hipConnection = hipConnection;
    	this.hipster = hipConnection.getHipster();

    	Query query;
    	
    	if(queryParts.length == 1 && queryParts[0] instanceof Query) query = (Query) queryParts[0];
    	else query = new Query(queryParts);
    	
        this.hipConnection.lastQuery = query;
        this.hipConnection.lastPrepared = hipster.prepare(query);
        
		try {
			ps = hipConnection.getConnection().prepareStatement(this.hipConnection.lastPrepared.getQueryString());
		} catch (SQLException e) {
			throw new RuntimeException("Error preparing statement: "+this.hipConnection.lastPrepared.getQueryString()+" ERR: "+e.getMessage(), e);
		}

		List<Object> params = this.hipConnection.lastPrepared.getParams();
		for(int i=0; i<params.size(); i++){
			try {
				hipster.prepSet(hipConnection,ps,i+1,params.get(i));
			} catch (SQLException e) {
				throw new RuntimeException("Error filling prepared statement: "+this.hipConnection.lastPrepared.getQueryString()+" on index "+(i+1)+" using value "+params.get(i)+" ERR: "+e.getMessage(), e);
			}
		}
    }

    /** Execute the provided query
     * 
     * @return self
     */
    public Result query(){
        try{
            rs = ps.executeQuery();
            metaData = rs.getMetaData();
            return this;
        }catch (Exception e){
            close(); throw queryError(e);
        }
    }

    private RuntimeException queryError(Exception e) {
        return new RuntimeException("Problem executing query \n"+this.hipConnection.lastQuery+", error: "+e.getMessage(), e);
    }

    public int updateAndClose(){
        try{
            int ret = ps.executeUpdate();
            close();
            return ret;
        }catch (Exception e){
            close(); 
            throw queryError(e);
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

    /** @return null (if no more rows) or map of columnName->value.*/
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
}