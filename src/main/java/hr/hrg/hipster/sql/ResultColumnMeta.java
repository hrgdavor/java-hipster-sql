package hr.hrg.hipster.sql;

import java.util.*;

public class ResultColumnMeta implements IColumnMeta {
	protected Class<?> entity;
	protected Class<?> type;
	protected String getterName;
	protected String columnName;
	protected IResultGetter<?> getter;
	private boolean primitive;
	private List<Class<?>> typeParams;
	private String tableName;
	private int ordinal;
	
	public ResultColumnMeta(
			Class<?> entity, 
			Class<?> type, 
			String getterName, 
			String name,
			String columnName,
			String tableName,
			boolean primitive,
			int ordinal,
			IResultGetter<?> getter,
			List<Class<?>> typeParams) {
		super();
		this.entity = entity;
		this.type = type;
		this.getterName = getterName;
		this.columnName = columnName;
		this.tableName = tableName;
		this.primitive = primitive;
		this.ordinal = ordinal;
		this.getter = getter;
		if(typeParams instanceof IResultGetter){
			this.typeParams = typeParams;
		}else{			
			this.typeParams = ImmutableList.safe(typeParams.toArray(new Class<?>[typeParams.size()]));
		}
	}

	public Class<?> getEntity() {
		return entity;
	}

	public Class<?> getType() {
		return type;
	}

	public String getGetterName() {
		return getterName;
	}

	public String getColumnName() {
		return columnName;
	}

	public IResultGetter<?> getGetter() {
		return getter;
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

}
