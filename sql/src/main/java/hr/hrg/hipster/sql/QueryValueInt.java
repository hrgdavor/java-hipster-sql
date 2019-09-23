package hr.hrg.hipster.sql;

import java.sql.*;

public final class QueryValueInt implements IQeuryValue{
	final int value;

	public QueryValueInt(int value) {
		this.value = value;
	}

	@Override
	public  void set(PreparedStatement ps, int index) throws SQLException {
		ps.setInt(index, value);
	}

	@Override
	public String toString() {
		return Integer.toString(value);
	}
}