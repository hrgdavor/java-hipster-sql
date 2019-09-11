package hr.hrg.hipster.sql;

import java.sql.*;

public interface ICustomType<T> {

	public T get(ResultSet rs, int index) throws SQLException;

	public void set(PreparedStatement ps, int index, T value) throws SQLException;

//	default String valueToQueryString(T value){
//		if(value == null) {
//			return "NULL";
//		}else {
//			value.toString();
//		}
//	}
}
