package hr.hrg.hipster.query;

import java.sql.*;

public interface IQueryValue {

	public Object getValue();

	public void set(PreparedStatement ps, int index) throws SQLException;
	
	public static final IQueryValue NULL = new IQueryValue() {
		@Override
		public final void set(PreparedStatement ps, int index) throws SQLException {
			ps.setNull(index, Types.INTEGER);
		}

		@Override
		public String toString() {
			return "NULL";
		}

		@Override
		public Object getValue() {
			return null;
		}
	};
}
