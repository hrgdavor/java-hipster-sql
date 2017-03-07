package hr.hrg.hipstersql.setter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class LongSetter implements PreparedSetter<Long>{

	@Override
	public void set(PreparedStatement ps, int index, Long value) throws SQLException {
		if(value == null) ps.setNull(index, Types.BIGINT);
		ps.setLong(index, value.longValue());
	}

}
