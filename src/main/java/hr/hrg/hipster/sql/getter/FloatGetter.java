package hr.hrg.hipster.sql.getter;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class FloatGetter implements IResultGetter<Float>{

	@Override
	public Float get(ResultSet rs, int index) throws SQLException {
		float ret = rs.getFloat(index);
		if(rs.wasNull()) return null;
		return ret;
	}

}
