package hr.hrg.hipster.sql;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

public class ReaderMeta <T, E extends BaseColumnMeta> implements IReadMeta<T, E>{

	private Class<T> entityClass;
	private Class<?>[] interfaces;
	private String tableName;
	private List<E> columns;
	private String columnNamesStr;
	private List<ICustomType<?>> getters;
	private QueryLiteral table;

	@SuppressWarnings("unchecked")
	public ReaderMeta(Class<T> entityClass, String tableName, List<E> columns, List<ICustomType<?>> getters) {
		this.entityClass = entityClass;
		this.tableName = tableName;
		this.table = new QueryLiteral(tableName, true); 
		this.getters = getters;
		E[] array = (E[]) columns.toArray(new BaseColumnMeta[columns.size()]);
		this.columns =  ImmutableList.safe(array);
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			if(i>0) b.append(',');
			b.append('"');
			b.append(array[i].getColumnName());
			b.append('"');
		}
		columnNamesStr = b.toString();
		interfaces = new Class[]{entityClass};
	}
	
	@Override
	public Class<T> getEntityClass() {
		return entityClass;
	}

	@Override
	public String getTableName() {
		return tableName;
	}
	
	@Override
	public QueryLiteral getTable() {
		return table;
	}

	@Override
	public int getColumnCount() {
		return columns.size();
	}

	@Override
	public List<E> getColumns() {
		return columns;
	}

	@Override
	public String getColumnNamesStr() {
		return columnNamesStr;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T fromResultSet(ResultSet rs) throws SQLException {
		Map<Object,Object> map = new HashMap<>();
		int i=0;
		for(E col:columns){
			i++;
			map.put(col.getGetterName(), getters.get(i-1).get(rs, i));
		}
		return (T) Proxy.newProxyInstance(entityClass.getClassLoader(), interfaces, new ResultProxyHandler(map));
	}

}
