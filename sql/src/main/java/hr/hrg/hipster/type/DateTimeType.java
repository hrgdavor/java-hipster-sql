package hr.hrg.hipster.type;

import java.sql.*;

import org.joda.time.*;

public class DateTimeType implements ICustomType<DateTime>{

	@Override
	public void set(PreparedStatement ps, int index, DateTime value) throws SQLException {
		if(value == null) 
			ps.setNull(index, Types.BIGINT);
		else
			ps.setLong(index, value.getMillis());
	}
	
	@Override
	public DateTime get(ResultSet rs, int index) throws SQLException {
		long ret = rs.getLong(index);
		if(rs.wasNull()) return null;
		
		return new DateTime(ret);
	}
}
