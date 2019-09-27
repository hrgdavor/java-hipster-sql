package hr.hrg.hipster.type;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class IntegerType implements ICustomType<Integer>{

	@Override
	public Integer get(ResultSet rs, int index) throws SQLException {
		int ret = rs.getInt(index);
		if(rs.wasNull()) return null;
		return ret;
	}

	@Override
	public void set(PreparedStatement ps, int index, Integer value) throws SQLException {
		if(value == null) 
			ps.setNull(index, Types.INTEGER);
		else
			ps.setInt(index, value.intValue());
	}

}
