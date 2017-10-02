package hr.hrg.hipster.sql.getter;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class IntegerGetter implements IResultGetter<Integer>{

	@Override
	public Integer get(ResultSet rs, int index) throws SQLException {
		int ret = rs.getInt(index);
		if(rs.wasNull()) return null;
		return ret;
	}

}
