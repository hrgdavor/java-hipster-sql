package hr.hrg.hipstersql.setter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class StringSetter implements PreparedSetter<String>{

	@Override
	public void set(PreparedStatement ps, int index, String value) throws SQLException {
		if(value == null) ps.setNull(index, Types.VARCHAR);
		ps.setString(index, value);
	}

}
