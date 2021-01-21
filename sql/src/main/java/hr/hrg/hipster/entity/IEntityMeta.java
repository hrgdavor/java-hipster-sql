package hr.hrg.hipster.entity;

import java.sql.*;
import java.util.*;

import hr.hrg.hipster.query.*;
import hr.hrg.hipster.type.*;


@SuppressWarnings("rawtypes")
public interface IEntityMeta<T,ID> extends IQueryLiteral{

	int ordinal();

	Class<T> getEntityClass();

	Class[] getImplClasses();
	
	String getEntityName();

	String getTableName();
	
	int getColumnCount();

	ColumnMeta<ID> getPrimaryColumn();

	List<? extends ColumnMeta> getColumns();

	ColumnMeta getColumn(String name);
	ColumnMeta getColumn(int ordinal);
	
	boolean containsColumn(String columnName);
	int getColumnOrdinal(String columnName);

	ICustomType<?> getTypeHandler(ColumnMeta column);
	ICustomType<?> getTypeHandler(int ordinal);

	
	ID entityGetPrimary(T instance);

	IUpdatable mutableCopy(Object v);
	T fromResultSet(ResultSet rs) throws SQLException;

	ColumnMeta getField(String columnName);
	boolean containsField(String columnName);
	int getFieldOrdinal(String columnName);
	
	Class<T> getImmutableClass();
	Class<T> getUpdateClass();
}
