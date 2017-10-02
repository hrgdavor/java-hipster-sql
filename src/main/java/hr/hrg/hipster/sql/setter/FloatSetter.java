package hr.hrg.hipster.sql.setter;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class FloatSetter implements IPreparedSetter<Float>{

	@Override
	public void set(PreparedStatement ps, int index, Float value) throws SQLException {
		if(value == null) ps.setNull(index, Types.FLOAT);
		ps.setFloat(index, value.floatValue());
	}

}
