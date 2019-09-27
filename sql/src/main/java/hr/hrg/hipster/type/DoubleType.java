package hr.hrg.hipster.type;

import java.sql.*;

public class DoubleType implements ICustomType<Double>{

	@Override
	public Double get(ResultSet rs, int index) throws SQLException {
		double ret = rs.getDouble(index);
		if(rs.wasNull()) return null;
		return ret;
		
	}
	
	@Override
	public void set(PreparedStatement ps, int index, Double value) throws SQLException {
		if(value == null) 
			ps.setNull(index, Types.DOUBLE);
		else
			ps.setDouble(index, value.doubleValue());
	}	

}
