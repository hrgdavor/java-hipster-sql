package hr.hrg.hipster.sql.setter;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class LongSetter implements IPreparedSetter<Long>{

	@Override
	public void set(PreparedStatement ps, int index, Long value) throws SQLException {
		if(value == null) ps.setNull(index, Types.BIGINT);
		ps.setLong(index, value.longValue());
	}

}
