package hr.hrg.hipstersql.setter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class IntegerSetter implements PreparedSetter<Integer>{

	@Override
	public void set(PreparedStatement ps, int index, Integer value) throws SQLException {
		if(value == null) ps.setNull(index, Types.INTEGER);
		ps.setInt(index, value.intValue());
	}

}
