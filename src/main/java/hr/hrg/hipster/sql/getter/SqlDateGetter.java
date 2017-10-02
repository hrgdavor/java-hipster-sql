package hr.hrg.hipster.sql.getter;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class SqlDateGetter implements IResultGetter<Date>{

	@Override
	public Date get(ResultSet rs, int index) throws SQLException {
		return rs.getDate(index);
	}

}
