package hr.hrg.hipster.type;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public interface ICustomType<T> {

	public T get(ResultSet rs, int index) throws SQLException;

	public void set(PreparedStatement ps, int index, T value) throws SQLException;

	default String valueToQueryString(T value){
		return HipsterSql.qValue(new StringBuilder(), value).toString();
	}
}
