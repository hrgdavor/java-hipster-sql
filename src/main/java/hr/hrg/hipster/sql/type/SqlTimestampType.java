package hr.hrg.hipster.sql.type;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class SqlTimestampType implements ICustomType<Timestamp>{

	@Override
	public Timestamp get(ResultSet rs, int index) throws SQLException {
		return rs.getTimestamp(index);
	}

	@Override
	public void set(PreparedStatement ps, int index, Timestamp value) throws SQLException {
		if(value == null) 
			ps.setNull(index, Types.TIMESTAMP);
		else
			ps.setTimestamp(index, value);
	}	
}
