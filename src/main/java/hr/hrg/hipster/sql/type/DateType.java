package hr.hrg.hipster.sql.type;

import java.sql.*;
import java.util.Date;

import hr.hrg.hipster.sql.*;

public class DateType implements ICustomType<Date>{

	@Override
	public Date get(ResultSet rs, int index) throws SQLException {
		java.sql.Date ret = rs.getDate(index);
		if(rs.wasNull()) return null;
		return new Date(ret.getTime());		
	}

	@Override
	public void set(PreparedStatement ps, int index, Date value) throws SQLException {
		if(value == null) 
			ps.setNull(index, Types.DATE);
		else
			ps.setDate(index, new java.sql.Date(value.getTime()));
	}	
	
}
