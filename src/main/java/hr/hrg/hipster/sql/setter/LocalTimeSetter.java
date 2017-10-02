package hr.hrg.hipster.sql.setter;

import java.sql.*;

import org.joda.time.*;

import hr.hrg.hipster.sql.*;

public class LocalTimeSetter implements IPreparedSetter<LocalTime>{

	@SuppressWarnings("deprecation")
	@Override
	public void set(PreparedStatement ps, int index, LocalTime value) throws SQLException {
		if(value == null) ps.setNull(index, Types.TIME);
		ps.setTime(index, new java.sql.Time(value.getHourOfDay(),value.getMinuteOfHour(), value.getSecondOfMinute()));
	}
}
