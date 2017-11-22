package hr.hrg.hipster.dao;

import hr.hrg.hipster.sql.*;


public interface IEntityMeta<T,ID,E extends IColumnMeta> extends IReadMeta<T,E>{

	Class<E> getEntityEnum();

	String getEntityName();
	
	boolean containsColumn(String columnName);
	
	E getPrimaryColumn();

	E getColumn(String name);
	
	E getColumn(int ordinal);

	ID entityGetPrimary(T instance);

	IUpdatable<E> mutableCopy(Object v);
}
