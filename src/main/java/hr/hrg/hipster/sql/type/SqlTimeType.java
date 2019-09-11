package hr.hrg.hipster.sql.type;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class SqlTimeType implements ICustomType<Time>{

	@Override
	public Time get(ResultSet rs, int index) throws SQLException {
		return rs.getTime(index);
	}

	@Override
	public void set(PreparedStatement ps, int index, Time value) throws SQLException {
		if(value == null) 
			ps.setNull(index, Types.TIME);
		else
			ps.setTime(index, value);
	}	
}
