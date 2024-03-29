package hr.hrg.hipster.query;

import java.sql.*;

public final class QueryValueLong implements IQueryValue{
	final long value;

	public QueryValueLong(long value) {
		this.value = value;
	}

	@Override
	public void set(PreparedStatement ps, int index) throws SQLException {
		ps.setLong(index, value);
	}

	@Override
	public String toString() {
		return Long.toString(value);
	}
	
	@Override
	public Object getValue() {
		return value;
	}
	
}