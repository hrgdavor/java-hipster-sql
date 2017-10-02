package hr.hrg.hipster.sql.setter;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class DoubleSetter implements IPreparedSetter<Double>{

	@Override
	public void set(PreparedStatement ps, int index, Double value) throws SQLException {
		if(value == null) ps.setNull(index, Types.DOUBLE);
		ps.setDouble(index, value.doubleValue());
	}

}
