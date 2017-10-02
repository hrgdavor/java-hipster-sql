package hr.hrg.hipster.sql.getter;

import java.sql.*;

import org.joda.time.*;

import hr.hrg.hipster.sql.*;

public class DateTimeGetter implements IResultGetter<DateTime>{

	@Override
	public DateTime get(ResultSet rs, int index) throws SQLException {
		Timestamp ret = rs.getTimestamp(index);
		if(rs.wasNull()) return null;
		return new DateTime(ret.getTime());
	}
}
