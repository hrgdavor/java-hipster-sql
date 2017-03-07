package hr.hrg.hipstersql.setter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.joda.time.LocalTime;

public class LocalTimeSetter implements PreparedSetter<LocalTime>{

	@SuppressWarnings("deprecation")
	@Override
	public void set(PreparedStatement ps, int index, LocalTime value) throws SQLException {
		if(value == null) ps.setNull(index, Types.TIME);
		ps.setTime(index, new java.sql.Time(value.getHourOfDay(),value.getMinuteOfHour(), value.getSecondOfMinute()));
	}
}
