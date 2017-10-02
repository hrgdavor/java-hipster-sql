package hr.hrg.hipster.sql;

import java.sql.*;

public interface IResultGetter<T> {
	public T get(ResultSet rs, int index) throws SQLException;
}
