package hr.hrg.hipster.sql;

import java.sql.*;

public interface ICustomType<T> {

	public T get(ResultSet rs, int index) throws SQLException;

	public void set(PreparedStatement ps, int index, T value) throws SQLException;
	
}
