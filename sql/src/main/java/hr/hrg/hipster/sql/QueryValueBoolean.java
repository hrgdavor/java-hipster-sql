package hr.hrg.hipster.sql;

import java.sql.*;

public final class QueryValueBoolean implements IQeuryValue{
	final boolean value;

	public QueryValueBoolean(boolean value) {
		this.value = value;
	}

	@Override
	public void set(PreparedStatement ps, int index) throws SQLException {
		ps.setBoolean(index, value);
	}

	@Override
	public String toString() {
		return Boolean.toString(value);
	}
}