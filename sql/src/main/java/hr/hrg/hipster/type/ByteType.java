package hr.hrg.hipster.type;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class ByteType implements ICustomType<Byte>{

	@Override
	public Byte get(ResultSet rs, int index) throws SQLException {
		byte ret = rs.getByte(index);
		if(rs.wasNull()) return null;
		return ret;
	}

	@Override
	public void set(PreparedStatement ps, int index, Byte value) throws SQLException {
		if(value == null) 
			ps.setNull(index, Types.FLOAT);
		else
			ps.setByte(index, value.byteValue());
	}

}
