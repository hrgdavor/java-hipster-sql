package hr.hrg.hipster.query;

import java.sql.*;

public final class QueryValueInt implements IQueryValue{
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
	
	@Override
	public Object getValue() {
		return value;
	}
	
}