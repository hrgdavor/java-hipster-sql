package hr.hrg.hipster.query;

import java.sql.*;

public final class QueryValueFloat implements IQueryValue{
	final float value;

	public QueryValueFloat(float value) {
		this.value = value;
	}

	@Override
	public void set(PreparedStatement ps, int index) throws SQLException {
		ps.setFloat(index, value);
	}

	@Override
	public String toString() {
		return Float.toString(value);
	}

	@Override
	public Object getValue() {
		return value;
	}
	
}