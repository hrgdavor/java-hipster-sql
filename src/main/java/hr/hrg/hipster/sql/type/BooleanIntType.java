package hr.hrg.hipster.sql.type;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class BooleanIntType implements ICustomType<Boolean>{

	@Override
	public Boolean get(ResultSet rs, int index) throws SQLException {
		boolean ret = rs.getInt(index) != 0;
		if(rs.wasNull()) return null;
		return ret;
	}

	@Override
	public void set(PreparedStatement ps, int index, Boolean value) throws SQLException {
		if(value == null) ps.setNull(index, Types.INTEGER);
		ps.setInt(index, value.booleanValue() ? 1:0);
	}

}
