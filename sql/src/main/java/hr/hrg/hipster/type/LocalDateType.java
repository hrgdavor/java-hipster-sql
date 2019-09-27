package hr.hrg.hipster.type;

import java.sql.*;

import org.joda.time.*;

import hr.hrg.hipster.sql.*;

public class LocalDateType implements ICustomType<LocalDate>{

	@Override
	public LocalDate get(ResultSet rs, int index) throws SQLException {
		Timestamp ret = rs.getTimestamp(index);
		if(rs.wasNull()) return null;
		return new LocalDate(ret.getTime());
	}
	
	@Override
	public void set(PreparedStatement ps, int index, LocalDate value) throws SQLException {
		if(value == null) 
			ps.setNull(index, Types.DATE);
		else
			ps.setDate(index, new java.sql.Date(value.toDateTimeAtStartOfDay().getMillis()));
	}	
}
