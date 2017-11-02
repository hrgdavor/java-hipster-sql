package hr.hrg.hipster.sql;

import java.util.*;

public class ResultColumnMeta implements IColumnMeta {
	protected Class<?> entity;
	protected Class<?> type;
	protected String getterName;
	protected String columnName;
	private boolean primitive;
	private List<Class<?>> typeParams;
	private String tableName;
	private int ordinal;
	private String columnSql;
	
	public ResultColumnMeta(
			Class<?> entity, 
			Class<?> type, 
			String getterName, 
			String name,
			String columnName,
			String columnSql,
			String tableName,
			boolean primitive,
			int ordinal,
			List<Class<?>> typeParams) {
		super();
		this.entity = entity;
		this.type = type;
		this.getterName = getterName;
		this.columnName = columnName;
		this.columnSql = columnSql;
		this.tableName = tableName;
		this.primitive = primitive;
		this.ordinal = ordinal;
		if(typeParams instanceof IResultGetter){
			this.typeParams = typeParams;
		}else{			
			this.typeParams = ImmutableList.safe(typeParams.toArray(new Class<?>[typeParams.size()]));
		}
	}

	public Class<?> getEntity() {
		return entity;
	}

	@Override
	public Class<?> getType() {
		return type;
	}

	public String getGetterName() {
		return getterName;
	}

	@Override
	public String getColumnName() {
		return columnName;
	}

	@Override
	public String getQueryText() {
		return columnName;
	}

	@Override
	public String name() {
		return columnName;
	}

	@Override
	public boolean isPrimitive() {
		return primitive;
	}

	@Override
	public int ordinal() {
		return ordinal;
	}

	@Override
	public boolean isGeneric() {
		return typeParams.size() > 0;
	}

	@Override
	public List<Class<?>> getTypeParams() {
		return typeParams;
	}

	@Override
	public String getTableName() {
		return tableName;
	}

	@Override
	public String getColumnSql() {
		return columnSql;
	}
	
	@Override
	public String toString() {
		return columnName;
	}
}
