package hr.hrg.hipster.type;

import java.sql.*;

public class ShortType implements ICustomType<Short>{

	@Override
	public void set(PreparedStatement ps, int index, Short value) throws SQLException {
		if(value == null) 
			ps.setNull(index, Types.FLOAT);
		else
			ps.setShort(index, value.shortValue());
	}

	@Override
	public Short get(ResultSet rs, int index) throws SQLException {
		short ret = rs.getShort(index);
		if(rs.wasNull()) return null;
		return ret;
	}
}
