package hr.hrg.hipster.sql.setter;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class SqlDateSetter implements IPreparedSetter<Date>{

	@Override
	public void set(PreparedStatement ps, int index, Date value) throws SQLException {
		if(value == null) ps.setNull(index, Types.DATE);
		ps.setDate(index, value);
	}

}
