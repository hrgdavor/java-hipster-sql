package hr.hrg.hipster.sql;

import java.sql.*;

public interface IPreparedSetter<T> {

	public void set(PreparedStatement ps, int index, T value) throws SQLException;

}
