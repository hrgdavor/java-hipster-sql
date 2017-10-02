package hr.hrg.hipster.sql;

import java.sql.*;
import java.util.*;

public interface IReadMeta<T, E extends IColumnMeta> {

	Class<T> getEntityClass();

	String getTableName();

	int getColumnCount();

	List<E> getColumns();
	
	String getColumnNamesStr();

	T fromResultSet(ResultSet rs) throws SQLException;
}
