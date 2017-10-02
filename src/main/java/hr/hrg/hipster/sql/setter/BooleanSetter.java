package hr.hrg.hipster.sql.setter;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class BooleanSetter implements IPreparedSetter<Boolean>{

	@Override
	public void set(PreparedStatement ps, int index, Boolean value) throws SQLException {
		if(value == null) ps.setNull(index, Types.BOOLEAN);
		ps.setBoolean(index, value.booleanValue());
	}

}
