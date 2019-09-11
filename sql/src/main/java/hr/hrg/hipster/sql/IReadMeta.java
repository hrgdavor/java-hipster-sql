package hr.hrg.hipster.sql;

import java.sql.*;
import java.util.*;

public interface IReadMeta<T, E extends BaseColumnMeta> {

	Class<T> getEntityClass();

	String getTableName();

	QueryLiteral getTable();

	int getColumnCount();

	List<E> getColumns();
	
	String getColumnNamesStr();

	T fromResultSet(ResultSet rs) throws SQLException;
}
