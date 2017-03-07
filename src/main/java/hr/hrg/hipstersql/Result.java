package hr.hrg.hipstersql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class Result{
    /**
	 * 
	 */
	private final HipsterSql hipsterSql;
	protected String query;
    protected ResultSet rs;
    protected PreparedStatement ps;
    private ResultSetMetaData metaData;

    public Result(HipsterSql hipsterSql, Object ... queryParts ){
    	this.hipsterSql = hipsterSql;
		Query query;
    	
    	if(queryParts.length == 1 && queryParts[0] instanceof Query) query = (Query) queryParts[0];
    	else query = new Query(queryParts);
    	
        this.hipsterSql.lastQuery = query;
        this.hipsterSql.lastPrepared = this.hipsterSql.prepare(query);
        
		try {
			ps = hipsterSql.getConnection().prepareStatement(this.hipsterSql.lastPrepared.getQueryString());
		} catch (SQLException e) {
			throw new RuntimeException("Error preparing statement: "+this.hipsterSql.lastPrepared.getQueryString()+" ERR: "+e.getMessage(), e);
		}

		List<Object> params = this.hipsterSql.lastPrepared.getParams();
		for(int i=0; i<params.size(); i++){
			try {
				this.hipsterSql.prepSet(ps,i+1,params.get(i));
			} catch (SQLException e) {
				throw new RuntimeException("Error filling prepared statement: "+this.hipsterSql.lastPrepared.getQueryString()+" on index "+(i+1)+" using value "+params.get(i)+" ERR: "+e.getMessage(), e);
			}
		}
    }

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
        return new RuntimeException("Problem executing query \n"+this.hipsterSql.lastQuery+", error: "+e.getMessage(), e);
    }

    public int updateAndClose(){
        try{
            int ret = ps.executeUpdate();
            close();
            return ret;
        }catch (Exception e){
            close(); throw queryError(e);
        }
    }

    public List<Object> fetchRow(){
        try{
            if(!rs.next()) return null;
            int count = metaData.getColumnCount();
            List<Object> row = new ArrayList<Object>();
            for(int i=1; i<=count; i++){
                row.add(this.hipsterSql.handleRsGet(rs,i,metaData.getColumnType(i), metaData.getColumnName(i)));
            }
            return row;
        }catch (Exception e){
            close(); throw queryError(e);
        }
    }

    /**
     * @return null if no more rows, map of columnName:value */
    public Map<Object,Object> fetchAssoc(){
        try{
            if(!rs.next()) return null;
            int count = metaData.getColumnCount();
            Map<Object, Object> row = new LinkedHashMap<>();
            for(int i=1; i<=count; i++){
                String columnName = metaData.getColumnName(i);
                row.put(columnName, this.hipsterSql.handleRsGet(rs,i,metaData.getColumnType(i), columnName));
            }
            return row;
        }catch (Exception e){
            close(); throw queryError(e);
        }
    }

    public void close() {
        if(ps != null)
            try{ ps.close(); } catch (Exception e) {HipsterSql.log.error("Error closing prepared statement "+this.hipsterSql.lastPrepared+" ERR:"+e.getMessage(),e);}
        if(rs != null)
            try{ rs.close();} catch (Exception e) {HipsterSql.log.error("Error closing resultset ERR:"+e.getMessage(),e);}
    }
}