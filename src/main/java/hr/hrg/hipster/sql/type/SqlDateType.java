package hr.hrg.hipster.sql.type;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class SqlDateType implements ICustomType<Date>{

	@Override
	public Date get(ResultSet rs, int index) throws SQLException {
		return rs.getDate(index);
	}

	@Override
	public void set(PreparedStatement ps, int index, Date value) throws SQLException {
		if(value == null) 
			ps.setNull(index, Types.DATE);
		else
			ps.setDate(index, value);
	}	
}
