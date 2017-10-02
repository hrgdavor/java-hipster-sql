package hr.hrg.hipster.sql.getter;

import java.sql.*;

import org.joda.time.*;

import hr.hrg.hipster.sql.*;

public class LocalDateGetter implements IResultGetter<LocalDate>{

	@Override
	public LocalDate get(ResultSet rs, int index) throws SQLException {
		Timestamp ret = rs.getTimestamp(index);
		if(rs.wasNull()) return null;
		return new LocalDate(ret.getTime());
	}
}
