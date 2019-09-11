package hr.hrg.hipster.sql.type;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class LongType implements ICustomType<Long>{

	@Override
	public Long get(ResultSet rs, int index) throws SQLException {
		long ret = rs.getLong(index);
		if(rs.wasNull()) return null;
		return ret;
	}
	
	@Override
	public void set(PreparedStatement ps, int index, Long value) throws SQLException {
		if(value == null) 
			ps.setNull(index, Types.BIGINT);
		else
			ps.setLong(index, value.longValue());
	}	

}
