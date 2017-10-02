package hr.hrg.hipster.sql.getter;

import java.sql.*;

import org.joda.time.*;

import hr.hrg.hipster.sql.*;

public class LocalTimeGetter implements IResultGetter<LocalTime>{

	@Override
	public LocalTime get(ResultSet rs, int index) throws SQLException {
		Time ret = rs.getTime(index);
		if(rs.wasNull()) return null;
		return new LocalTime(ret.getTime());
	}
}
