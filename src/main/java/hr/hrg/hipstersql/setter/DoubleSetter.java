package hr.hrg.hipstersql.setter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class DoubleSetter implements PreparedSetter<Double>{

	@Override
	public void set(PreparedStatement ps, int index, Double value) throws SQLException {
		if(value == null) ps.setNull(index, Types.DOUBLE);
		ps.setDouble(index, value.doubleValue());
	}

}
