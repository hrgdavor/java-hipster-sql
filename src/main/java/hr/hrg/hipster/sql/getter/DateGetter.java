package hr.hrg.hipster.sql.getter;

import java.sql.*;
import java.util.Date;

import hr.hrg.hipster.sql.*;

public class DateGetter implements IResultGetter<Date>{

	@Override
	public Date get(ResultSet rs, int index) throws SQLException {
		java.sql.Date ret = rs.getDate(index);
		if(rs.wasNull()) return null;
		return new Date(ret.getTime());		
	}

}
