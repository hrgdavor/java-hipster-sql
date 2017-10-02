package hr.hrg.hipster.sql.getter;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class BooleanGetter implements IResultGetter<Boolean>{

	@Override
	public Boolean get(ResultSet rs, int index) throws SQLException {
		boolean ret = rs.getBoolean(index);
		if(rs.wasNull()) return null;
		return ret;
	}

}
