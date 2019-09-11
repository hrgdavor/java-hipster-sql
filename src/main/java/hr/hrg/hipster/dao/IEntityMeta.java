package hr.hrg.hipster.dao;

import hr.hrg.hipster.sql.*;


@SuppressWarnings("rawtypes")
public interface IEntityMeta<T,ID,E extends BaseColumnMeta> extends IReadMeta<T,E>{

	String getEntityName();
	
	boolean containsColumn(String columnName);
	
	E getPrimaryColumn();

	E getColumn(String name);

	int getColumnOrdinal(String columnName);
	
	E getColumn(int ordinal);

	ID entityGetPrimary(T instance);

	IUpdatable mutableCopy(Object v);
	
	int ordinal();

	ICustomType<?> getTypeHandler(E column);

	ICustomType<?> getTypeHandler(int ordinal);

}
