package hr.hrg.hipster.type;

import java.sql.*;
import java.util.*;

public class EnumType<T extends Enum<T>> implements ICustomType<T>{

	private Class<T> enumClass;
	private Map<String, T> map;
	public EnumType(Class<T> enumClass) {
		this.enumClass = enumClass;
		T[] enumConstants = enumClass.getEnumConstants();
		map = new HashMap<>(enumConstants.length*3);
		for (T t : enumConstants) {
			map.put(t.name(), t);
		}
	}

	@Override
	public T get(ResultSet rs, int index) throws SQLException {
		String ret = rs.getString(index);
		if(rs.wasNull()) return null;
		return map.get(ret);
	}

	@Override
	public void set(PreparedStatement ps, int index, T value) throws SQLException {
		if(value == null) ps.setNull(index, Types.VARCHAR);
		ps.setString(index, value.name());
	}

}
