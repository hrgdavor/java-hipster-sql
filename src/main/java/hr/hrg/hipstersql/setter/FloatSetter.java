package hr.hrg.hipstersql.setter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class FloatSetter implements PreparedSetter<Float>{

	@Override
	public void set(PreparedStatement ps, int index, Float value) throws SQLException {
		if(value == null) ps.setNull(index, Types.FLOAT);
		ps.setFloat(index, value.floatValue());
	}

}
