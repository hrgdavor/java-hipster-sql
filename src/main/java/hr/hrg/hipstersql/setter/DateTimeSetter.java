package hr.hrg.hipstersql.setter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.joda.time.DateTime;

public class DateTimeSetter implements PreparedSetter<DateTime>{

	@Override
	public void set(PreparedStatement ps, int index, DateTime value) throws SQLException {
		if(value == null) ps.setNull(index, Types.TIMESTAMP);
		ps.setTimestamp(index, new java.sql.Timestamp(value.getMillis()));
	}
}
