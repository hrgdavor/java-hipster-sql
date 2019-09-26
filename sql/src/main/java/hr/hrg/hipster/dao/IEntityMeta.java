package hr.hrg.hipster.dao;

import java.sql.*;
import java.util.*;

import hr.hrg.hipster.sql.*;


@SuppressWarnings("rawtypes")
public interface IEntityMeta<T,ID> extends IReadMeta<T>{

	String getEntityName();
	
	boolean containsColumn(String columnName);
	
	ColumnMeta<ID> getPrimaryColumn();

	ColumnMeta getColumn(String name);

	int getColumnOrdinal(String columnName);
	
	ColumnMeta getColumn(int ordinal);

	ID entityGetPrimary(T instance);

	IUpdatable mutableCopy(Object v);
	
	int ordinal();

	ICustomType<?> getTypeHandler(ColumnMeta column);

	ICustomType<?> getTypeHandler(int ordinal);

	Class<T> getEntityClass();

	String getTableName();

	int getColumnCount();

	List<? extends ColumnMeta> getColumns();
	
	T fromResultSet(ResultSet rs) throws SQLException;	
	
}
