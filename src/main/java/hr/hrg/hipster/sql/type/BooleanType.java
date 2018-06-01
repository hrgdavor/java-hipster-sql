package hr.hrg.hipster.sql.type;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class BooleanType implements ICustomType<Boolean>{

	@Override
	public Boolean get(ResultSet rs, int index) throws SQLException {
		boolean ret = rs.getBoolean(index);
		if(rs.wasNull()) return null;
		return ret;
	}

	@Override
	public void set(PreparedStatement ps, int index, Boolean value) throws SQLException {
		if(value == null) ps.setNull(index, Types.BOOLEAN);
		ps.setBoolean(index, value.booleanValue());
	}
}
