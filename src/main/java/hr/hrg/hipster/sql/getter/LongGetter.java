package hr.hrg.hipster.sql.getter;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class LongGetter implements IResultGetter<Long>{

	@Override
	public Long get(ResultSet rs, int index) throws SQLException {
		long ret = rs.getLong(index);
		if(rs.wasNull()) return null;
		return ret;
	}

}
