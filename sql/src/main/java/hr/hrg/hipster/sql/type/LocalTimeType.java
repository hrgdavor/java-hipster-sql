package hr.hrg.hipster.sql.type;

import java.sql.*;

import org.joda.time.*;

import hr.hrg.hipster.sql.*;

public class LocalTimeType implements ICustomType<LocalTime>{

	@Override
	public LocalTime get(ResultSet rs, int index) throws SQLException {
		Time ret = rs.getTime(index);
		if(rs.wasNull()) return null;
		return new LocalTime(ret.getTime());
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void set(PreparedStatement ps, int index, LocalTime value) throws SQLException {
		if(value == null) 
			ps.setNull(index, Types.TIME);
		else
			ps.setTime(index, new java.sql.Time(value.getHourOfDay(),value.getMinuteOfHour(), value.getSecondOfMinute()));
	}	
}
