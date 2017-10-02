package hr.hrg.hipster.sql.setter;

import java.sql.*;

import org.joda.time.*;

import hr.hrg.hipster.sql.*;

public class LocalDateSetter implements IPreparedSetter<LocalDate>{

	@Override
	public void set(PreparedStatement ps, int index, LocalDate value) throws SQLException {
		if(value == null) ps.setNull(index, Types.DATE);
		ps.setDate(index, new java.sql.Date(value.toDateTimeAtStartOfDay().getMillis()));
	}
}
