package hr.hrg.hipster.type;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class StringType implements ICustomType<String>{

	@Override
	public String get(ResultSet rs, int index) throws SQLException {
		return rs.getString(index);
	}

	@Override
	public void set(PreparedStatement ps, int index, String value) throws SQLException {
		if(value == null) 
			ps.setNull(index, Types.VARCHAR);
		else
			ps.setString(index, value);
	}	
}
