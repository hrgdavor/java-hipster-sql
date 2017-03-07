package hr.hrg.hipstersql.setter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class BooleanSetter implements PreparedSetter<Boolean>{

	@Override
	public void set(PreparedStatement ps, int index, Boolean value) throws SQLException {
		if(value == null) ps.setNull(index, Types.BOOLEAN);
		ps.setBoolean(index, value.booleanValue());
	}

}
