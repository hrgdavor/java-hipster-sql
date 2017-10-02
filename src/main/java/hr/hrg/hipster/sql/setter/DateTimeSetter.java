package hr.hrg.hipster.sql.setter;

import java.sql.*;

import org.joda.time.*;

import hr.hrg.hipster.sql.*;

public class DateTimeSetter implements IPreparedSetter<DateTime>{

	@Override
	public void set(PreparedStatement ps, int index, DateTime value) throws SQLException {
		if(value == null) ps.setNull(index, Types.TIMESTAMP);
		ps.setTimestamp(index, new java.sql.Timestamp(value.getMillis()));
	}
}
