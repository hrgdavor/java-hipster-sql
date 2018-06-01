package hr.hrg.hipster.dao;

import hr.hrg.hipster.sql.*;


public interface IEntityMeta<T,ID,E extends BaseColumnMeta> extends IReadMeta<T,E>{

	String getEntityName();
	
	boolean containsColumn(String columnName);
	
	E getPrimaryColumn();

	E getColumn(String name);
	
	E getColumn(int ordinal);

	ID entityGetPrimary(T instance);

	IUpdatable<E> mutableCopy(Object v);
	
	int ordinal();

	ICustomType<?> getTypeHandler(E column);

	ICustomType<?> getTypeHandler(int ordinal);
}
