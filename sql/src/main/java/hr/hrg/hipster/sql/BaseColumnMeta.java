package hr.hrg.hipster.sql;

import java.lang.annotation.*;
import java.lang.reflect.*;

@SuppressWarnings("rawtypes")
public class BaseColumnMeta<T> implements IQueryLiteral, Key<T>, Comparable<BaseColumnMeta>{

	protected final Class<T> type;
	protected final Class<?> primitiveType;
	protected final String columnName;
	protected final String getterName;
	private IReadMeta meta;
	protected final String columnSql;
	protected final ImmutableList<Class<?>> typeParams;
	protected final int ordinal;
	protected final String name;
	protected final int hashCode;
	protected Annotation[] annotations;
	private ICustomType<?> typeHandler;

	public BaseColumnMeta(
			int ordinal, 
			String _name, 
			String _columnName, 
			String _getterName,
			IReadMeta meta,
			Class<T> _type,
			Class<?> _primitiveType, 
			String _columnSql, 
			ICustomType<?> typeHandler,
			Class<?>... typeParams){
		this.ordinal = ordinal;
		this.name = _name;
		this.columnName = _columnName;
		this.getterName = _getterName;
		this.meta = meta;
		this.type = _type;
		this.typeHandler = typeHandler;
		this.primitiveType = _primitiveType == _type ? null:_primitiveType;
		this.columnSql = _columnSql;
		this.typeParams = ImmutableList.safe(typeParams);
		this.hashCode = meta.getEntityClass().hashCode() * 31 + ordinal;
		
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

	public IReadMeta getMeta() {
		return meta;
	}

	public String getTableName() {
		return meta.getTableName();
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

	public ICustomType<?> getTypeHandler() {
		return typeHandler;
	}
	
	public BaseColumnMeta<T> withAnnotations(Annotation ... annotations){
		this.annotations = annotations;
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public <A extends Annotation> A getAnnotation(Class<A> clazz){
		if(annotations == null) {			
			try {
				
				Method method = meta.getEntityClass().getMethod(getterName);
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
		int first = this.meta.getTableName().compareTo(meta.getTableName());
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
			return meta.ordinal() == ordinal && meta.getEntity() == this.getEntity();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
	
	
	/* IQueryLiteral */
	
	@Override
	public String getQueryText() {
		return columnName;
	}

	@Override
	public boolean isIdentifier() {
		return true;
	}
		
	public Class<?> getEntity() {
		return meta.getEntityClass();
	}
	
	public final String toString() {
		return columnName;
	}
	

	
}
