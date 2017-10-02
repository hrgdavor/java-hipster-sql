package hr.hrg.hipster.sql.getter;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class ByteGetter implements IResultGetter<Byte>{

	@Override
	public Byte get(ResultSet rs, int index) throws SQLException {
		byte ret = rs.getByte(index);
		if(rs.wasNull()) return null;
		return ret;
	}

}
