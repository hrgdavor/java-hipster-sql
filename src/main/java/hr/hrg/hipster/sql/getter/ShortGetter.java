package hr.hrg.hipster.sql.getter;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class ShortGetter implements IResultGetter<Short>{

	@Override
	public Short get(ResultSet rs, int index) throws SQLException {
		short ret = rs.getShort(index);
		if(rs.wasNull()) return null;
		return ret;
	}

}
