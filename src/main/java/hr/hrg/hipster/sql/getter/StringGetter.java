package hr.hrg.hipster.sql.getter;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class StringGetter implements IResultGetter<String>{

	@Override
	public String get(ResultSet rs, int index) throws SQLException {
		return rs.getString(index);
	}

}
