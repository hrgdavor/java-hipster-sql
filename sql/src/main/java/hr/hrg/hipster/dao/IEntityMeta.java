package hr.hrg.hipster.dao;

import hr.hrg.hipster.sql.*;


@SuppressWarnings("rawtypes")
public interface IEntityMeta<T,ID> extends IReadMeta<T>{

	String getEntityName();
	
	boolean containsColumn(String columnName);
	
	BaseColumnMeta<ID> getPrimaryColumn();

	BaseColumnMeta getColumn(String name);

	int getColumnOrdinal(String columnName);
	
	BaseColumnMeta getColumn(int ordinal);

	ID entityGetPrimary(T instance);

	IUpdatable mutableCopy(Object v);
	
	int ordinal();

	ICustomType<?> getTypeHandler(BaseColumnMeta column);

	ICustomType<?> getTypeHandler(int ordinal);

	
	
}
