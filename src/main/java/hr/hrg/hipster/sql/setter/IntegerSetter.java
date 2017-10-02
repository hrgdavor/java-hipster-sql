package hr.hrg.hipster.sql.setter;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class IntegerSetter implements IPreparedSetter<Integer>{

	@Override
	public void set(PreparedStatement ps, int index, Integer value) throws SQLException {
		if(value == null) ps.setNull(index, Types.INTEGER);
		ps.setInt(index, value.intValue());
	}

}
