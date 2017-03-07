package hr.hrg.hipstersql.setter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.Date;

public class SqlDateSetter implements PreparedSetter<Date>{

	@Override
	public void set(PreparedStatement ps, int index, Date value) throws SQLException {
		if(value == null) ps.setNull(index, Types.DATE);
		ps.setDate(index, value);
	}

}
