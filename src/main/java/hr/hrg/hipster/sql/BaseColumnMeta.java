package hr.hrg.hipster.sql;

public class BaseColumnMeta<T> implements IColumnMeta, IQueryLiteral, Key<T>, Comparable<IColumnMeta>{

	protected final Class<?> entity;
	protected final Class<T> type;
	protected final Class<?> primitiveType;
	protected final String columnName;
	protected final String getterName;
	protected final String tableName;
	protected final String columnSql;
	protected final ImmutableList<Class<?>> typeParams;
	protected final int ordinal;
	protected final String name;
	protected final int hashCode;

	public BaseColumnMeta(
			int ordinal, 
			String _name, 
			String _columnName, 
			String _getterName, 
			Class<?> _entity,
			Class<T> _type,
			Class<?> _primitiveType, 
			String _tableName, 
			String _columnSql, 
			Class<?>... typeParams){
		this.ordinal = ordinal;
		this.name = _name;
		this.columnName = _columnName;
		this.getterName = _getterName;
		this.entity = _entity;
		this.type = _type;
		this.primitiveType = _primitiveType == _type ? null:_primitiveType;
		this.tableName = _tableName;
		this.columnSql = _columnSql;
		this.typeParams = ImmutableList.safe(typeParams);
		this.hashCode = _entity.hashCode() * 31 + ordinal;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public int ordinal() {
		return ordinal;
	}

	@Override
	public Class<T> getType() {
		return type;
	}

	@Override
	public Class<?> getPrimitiveType() {
		return primitiveType;
	}

	@Override
	public boolean isPrimitive() {
		return primitiveType != null;
	}

	@Override
	public String getColumnName() {
		return columnName;
	}

	@Override
	public String getGetterName() {
		return getterName;
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
	public ImmutableList<Class<?>> getTypeParams() {
		return typeParams;
	}

	@Override
	public boolean isGeneric() {
		return typeParams.isEmpty();
	}

	@Override
	public String getQueryText() {
		return columnName;
	}

	@Override
	public boolean isIdentifier() {
		return true;
	}

	@Override
	public Class<?> getEntity() {
		return entity;
	}
	
	@Override
	public final String toString() {
		return columnName;
	}
	 
	@Override
	public int compareTo(IColumnMeta o) {
		if(o == null) throw new NullPointerException();
		IColumnMeta meta = (IColumnMeta) o;
		int first = tableName.compareTo(meta.getTableName());
		if(first == 0) {
			return name.compareTo(meta.name());
		}
		
		return first;
	}
	  
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null) return false;
		if(obj instanceof IColumnMeta) {
			IColumnMeta meta = (IColumnMeta) obj;
			return meta.ordinal() == ordinal && meta.getEntity() == entity;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
	
	
}
