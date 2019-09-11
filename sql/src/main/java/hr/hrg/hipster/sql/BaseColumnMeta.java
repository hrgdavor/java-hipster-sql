package hr.hrg.hipster.sql;

import java.lang.annotation.*;
import java.lang.reflect.*;

@SuppressWarnings("rawtypes")
public class BaseColumnMeta<T> implements IQueryLiteral, Key<T>, Comparable<BaseColumnMeta>{

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
	protected Annotation[] annotations;

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

	public String name() {
		return name;
	}

	public int ordinal() {
		return ordinal;
	}

	public Class<T> getType() {
		return type;
	}

	public Class<?> getPrimitiveType() {
		return primitiveType;
	}

	public boolean isPrimitive() {
		return primitiveType != null;
	}

	public String getColumnName() {
		return columnName;
	}

	public String getGetterName() {
		return getterName;
	}

	public String getTableName() {
		return tableName;
	}

	public String getColumnSql() {
		return columnSql;
	}

	public ImmutableList<Class<?>> getTypeParams() {
		return typeParams;
	}

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
	public boolean isEmpty() {
		return false;
	}

	public Class<?> getEntity() {
		return entity;
	}
	
	public final String toString() {
		return columnName;
	}

	public BaseColumnMeta<T> withAnnotations(Annotation ... annotations){
		this.annotations = annotations;
		return this;
	}
	
	public <A extends Annotation> A getAnnotation(Class<A> clazz){
		if(annotations == null) {			
			try {
				Method method = entity.getMethod(getterName);
				annotations = method.getAnnotations();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for(Annotation tmp: annotations) {
			if(tmp.annotationType() == clazz) return (A) tmp;
		}

		return null;
	}
	
	@Override
	public int compareTo(BaseColumnMeta o) {
		if(o == null) throw new NullPointerException();
		BaseColumnMeta meta = (BaseColumnMeta) o;
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
		if(obj instanceof BaseColumnMeta) {
			BaseColumnMeta meta = (BaseColumnMeta) obj;
			return meta.ordinal() == ordinal && meta.getEntity() == entity;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
	
}
