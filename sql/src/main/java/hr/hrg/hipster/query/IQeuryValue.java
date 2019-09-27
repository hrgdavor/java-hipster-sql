package hr.hrg.hipster.query;

import java.sql.*;

public interface IQeuryValue {


	public void set(PreparedStatement ps, int index) throws SQLException;
	

	
	public static final IQeuryValue NULL = new IQeuryValue() {
		@Override
		public final void set(PreparedStatement ps, int index) throws SQLException {
			ps.setNull(index, Types.INTEGER);
		}

		@Override
		public String toString() {
			return "NULL";
		}
	};
}
