package hr.hrg.hipster.sql;

import java.sql.*;
import java.util.*;

public interface IReadMeta<T> extends IQueryLiteral {

	Class<T> getEntityClass();

	String getTableName();

	int getColumnCount();

	List<? extends BaseColumnMeta> getColumns();
	
	T fromResultSet(ResultSet rs) throws SQLException;
}
