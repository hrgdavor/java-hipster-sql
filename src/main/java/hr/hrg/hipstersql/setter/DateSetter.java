package hr.hrg.hipstersql.setter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

public class DateSetter implements PreparedSetter<Date>{

	@Override
	public void set(PreparedStatement ps, int index, Date value) throws SQLException {
		if(value == null) ps.setNull(index, Types.DATE);
		ps.setDate(index, new java.sql.Date(value.getTime()));
	}

}
