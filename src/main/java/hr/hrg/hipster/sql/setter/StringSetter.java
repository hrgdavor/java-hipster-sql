package hr.hrg.hipster.sql.setter;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class StringSetter implements IPreparedSetter<String>{

	@Override
	public void set(PreparedStatement ps, int index, String value) throws SQLException {
		if(value == null) ps.setNull(index, Types.VARCHAR);
		ps.setString(index, value);
	}

}
