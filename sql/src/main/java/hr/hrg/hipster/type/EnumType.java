package hr.hrg.hipster.type;

import java.sql.*;

public class EnumType<T extends Enum<T>> implements ICustomType<T>{

	private Class<T> enumClass;

	public EnumType(Class<T> enumClass) {
		this.enumClass = enumClass;
	}

	@Override
	public T get(ResultSet rs, int index) throws SQLException {
		String ret = rs.getString(index);
		if(rs.wasNull()) return null;
		return Enum.valueOf(enumClass, ret);
	}

	@Override
	public void set(PreparedStatement ps, int index, T value) throws SQLException {
		if(value == null) ps.setNull(index, Types.VARCHAR);
		ps.setString(index, value.name());
	}

}
