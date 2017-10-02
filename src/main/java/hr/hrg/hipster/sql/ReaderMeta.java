package hr.hrg.hipster.sql;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

public class ReaderMeta <T> implements IReadMeta<T, ResultColumnMeta>{

	private Class<T> entityClass;
	private Class<?>[] interfaces;
	private String tableName;
	private List<ResultColumnMeta> columns;
	private String columnNamesStr;

	public ReaderMeta(Class<T> entityClass, String tableName, List<ResultColumnMeta> columns) {
		this.entityClass = entityClass;
		this.tableName = tableName;
		ResultColumnMeta[] array = columns.toArray(new ResultColumnMeta[columns.size()]);
		this.columns = ImmutableList.safe(array);
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
	public int getColumnCount() {
		return columns.size();
	}

	@Override
	public List<ResultColumnMeta> getColumns() {
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
		for(ResultColumnMeta col:columns){
			i++;
			map.put(col.getGetterName(), col.getter.get(rs, i));
		}
		return (T) Proxy.newProxyInstance(entityClass.getClassLoader(), interfaces, new ResultProxyHandler(map));
	}

}
