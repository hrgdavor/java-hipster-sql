package hr.hrg.hipster.sql.setter;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class ShortSetter implements IPreparedSetter<Short>{

	@Override
	public void set(PreparedStatement ps, int index, Short value) throws SQLException {
		if(value == null) ps.setNull(index, Types.FLOAT);
		ps.setShort(index, value.shortValue());
	}

}
