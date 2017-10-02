package hr.hrg.hipster.sql.setter;

import java.sql.*;
import java.util.Date;

import hr.hrg.hipster.sql.*;

public class DateSetter implements IPreparedSetter<Date>{

	@Override
	public void set(PreparedStatement ps, int index, Date value) throws SQLException {
		if(value == null) ps.setNull(index, Types.DATE);
		ps.setDate(index, new java.sql.Date(value.getTime()));
	}

}
