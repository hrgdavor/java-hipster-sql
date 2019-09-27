package hr.hrg.hipster.type;

import java.sql.*;
import java.util.Date;

public class DateType implements ICustomType<Date>{

	@Override
	public Date get(ResultSet rs, int index) throws SQLException {
		java.sql.Timestamp ret = rs.getTimestamp(index);
		if(rs.wasNull()) return null;
		return new Date(ret.getTime());
	}

	@Override
	public void set(PreparedStatement ps, int index, Date value) throws SQLException {
		if(value == null) 
			ps.setNull(index, Types.DATE);
		else
			ps.setTimestamp(index, new java.sql.Timestamp(value.getTime()));
	}	
	
}
