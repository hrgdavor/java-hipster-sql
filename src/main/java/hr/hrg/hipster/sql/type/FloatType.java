package hr.hrg.hipster.sql.type;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class FloatType implements ICustomType<Float>{

	@Override
	public Float get(ResultSet rs, int index) throws SQLException {
		float ret = rs.getFloat(index);
		if(rs.wasNull()) return null;
		return ret;
	}

	@Override
	public void set(PreparedStatement ps, int index, Float value) throws SQLException {
		if(value == null) 
			ps.setNull(index, Types.FLOAT);
		else
			ps.setFloat(index, value.floatValue());
	}
	
}
