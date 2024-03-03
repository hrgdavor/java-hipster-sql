package hr.hrg.hipster.query;

import java.sql.*;

public final class QueryValueDouble implements IQueryValue{
	final double value;

	public QueryValueDouble(double value) {
		this.value = value;
	}

	@Override
	public void set(PreparedStatement ps, int index) throws SQLException {
		ps.setDouble(index, value);
	}

	@Override
	public String toString() {
		return Double.toString(value);
	}

	@Override
	public Object getValue() {
		return value;
	}

}