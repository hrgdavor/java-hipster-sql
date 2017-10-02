package hr.hrg.hipster.sql.setter;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class ByteSetter implements IPreparedSetter<Byte>{

	@Override
	public void set(PreparedStatement ps, int index, Byte value) throws SQLException {
		if(value == null) ps.setNull(index, Types.FLOAT);
		ps.setByte(index, value.byteValue());
	}

}
