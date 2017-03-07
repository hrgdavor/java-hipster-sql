package hr.hrg.hipstersql.setter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.joda.time.LocalDate;

public class LocalDateSetter implements PreparedSetter<LocalDate>{

	@Override
	public void set(PreparedStatement ps, int index, LocalDate value) throws SQLException {
		if(value == null) ps.setNull(index, Types.DATE);
		ps.setDate(index, new java.sql.Date(value.toDate().getTime()));
	}
}
