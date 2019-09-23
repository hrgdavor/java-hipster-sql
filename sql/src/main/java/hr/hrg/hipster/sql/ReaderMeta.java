package hr.hrg.hipster.sql;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("rawtypes")
public class ReaderMeta <T> implements IReadMeta<T>{

	private Class<T> entityClass;
	private Class<?>[] interfaces;
	private String tableName;
	private List<BaseColumnMeta> columns;
	private List<ICustomType<?>> getters;

	@SuppressWarnings("unchecked")
	public ReaderMeta(Class<T> entityClass, String tableName, List<BaseColumnMeta> columns, List<ICustomType<?>> getters) {
		this.entityClass = entityClass;
		this.tableName = tableName;
		this.getters = getters;
		BaseColumnMeta[] array = (BaseColumnMeta[]) columns.toArray(new BaseColumnMeta[columns.size()]);
		this.columns =  ImmutableList.safe(array);
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
	public List<BaseColumnMeta> getColumns() {
		return columns;
	}

	@Override
	public CharSequence getQueryText() {
		return tableName;
	}
	
	@Override
	public boolean isIdentifier() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T fromResultSet(ResultSet rs) throws SQLException {
		Map<Object,Object> map = new HashMap<>();
		int i=0;
		for(BaseColumnMeta col:columns){
			i++;
			map.put(col.getGetterName(), getters.get(i-1).get(rs, i));
		}
		return (T) Proxy.newProxyInstance(entityClass.getClassLoader(), interfaces, new ResultProxyHandler(map));
	}

}
