package hr.hrg.hipster.entity;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

import hr.hrg.hipster.query.*;
import hr.hrg.hipster.sql.*;
import hr.hrg.hipster.type.*;

@SuppressWarnings("rawtypes")
public class ColumnMeta<T> implements IQueryLiteral, Key<T>, Comparable<ColumnMeta>{

	protected final Class<T> type;
	protected final Class<?> primitiveType;
	protected final String columnName;
	protected final String getterName;
	private IEntityMeta meta;
	protected final String columnSql;
	protected final ImmutableList<Class<?>> typeParams;
	protected final int ordinal;
	protected final String name;
	protected final int hashCode;
	protected Annotation[] annotations;
	private ICustomType<?> typeHandler;
	protected boolean required;

	public ColumnMeta(
			int ordinal, 
			String _name, 
			String _columnName, 
			String _getterName,
			boolean _required,
			IEntityMeta meta,
			Class<T> _type,
			Class<?> _primitiveType, 
			String _columnSql, 
			ICustomType<?> typeHandler,
			Class<?>... typeParams){
		this.ordinal = ordinal;
		this.name = _name;
		this.columnName = _columnName;
		this.getterName = _getterName;
		this.required = _required;
		this.meta = meta;
		this.type = _type;
		this.typeHandler = typeHandler;
		this.primitiveType = _primitiveType == _type ? null:_primitiveType;
		this.columnSql = _columnSql;
		this.typeParams = ImmutableList.safe(typeParams);
		this.hashCode = meta.getEntityClass().hashCode() * 31 + ordinal;
		
	}

	public final String name() {
		return name;
	}

	public final int ordinal() {
		return ordinal;
	}

	public Class<T> getType() {
		return type;
	}

	public final Class<?> getPrimitiveType() {
		return primitiveType;
	}

	public final boolean isPrimitive() {
		return primitiveType != null;
	}

	public final String getColumnName() {
		return columnName;
	}

	public final String getGetterName() {
		return getterName;
	}

	public final IEntityMeta getMeta() {
		return meta;
	}

	public final String getTableName() {
		return meta.getTableName();
	}

	public final String getColumnSql() {
		return columnSql;
	}

	public final ImmutableList<Class<?>> getTypeParams() {
		return typeParams;
	}

	public final boolean isGeneric() {
		return typeParams.isEmpty();
	}

	public final ICustomType<?> getTypeHandler() {
		return typeHandler;
	}
	
	public ColumnMeta<T> withAnnotations(Annotation ... annotations){
		this.annotations = annotations;
		return this;
	}
	
	public QueryColumnAndValue op(CharSequence op, T value) {
		return new QueryColumnAndValue(this, String.format("%s?",op), new QueryValue<T>(value, (ICustomType<T>) typeHandler));
	}
	
	public QueryColumnAndValue in(T ...values) {
		return in("", values);
	}
	
	public QueryColumnAndValue inIfNotEmpty(CharSequence prefix, T ...values) {
		if(values.length == 0) return QueryColumnAndValue.EMPTY;
		return in("", values);
	}
	
	public QueryColumnAndValue in(CharSequence prefix, T ...values) {
		QueryValue[] valuesForQ = new QueryValue[values.length];
		StringBuilder b = new StringBuilder().append(" IN(");
		for(int i=0; i<values.length; i++) {
			if(i>0) b.append(",");
			b.append("?");
			valuesForQ[i] = new QueryValue(values[i], (ICustomType<T>) typeHandler);
		}
		b.append(")");
		
		return new QueryColumnAndValue(prefix, this, b, valuesForQ);
	}
	
	public QueryColumnAndValue inList(List<T> values) {
		return inList("", values);
	}
	
	public QueryColumnAndValue inList(CharSequence prefix, List<T> values) {
		QueryValue[] valuesForQ = new QueryValue[values.size()];
		StringBuilder b = new StringBuilder().append(" IN(");
		for(int i=0; i<values.size(); i++) {
			if(i>0) b.append(",");
			b.append("?");
			valuesForQ[i] = new QueryValue(values.get(i), (ICustomType<T>) typeHandler);
		}
		b.append(")");
		
		return new QueryColumnAndValue(prefix, this, b, valuesForQ);
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
	public int compareTo(ColumnMeta o) {
		if(o == null) throw new NullPointerException();
		ColumnMeta meta = (ColumnMeta) o;
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
		if(obj instanceof ColumnMeta) {
			ColumnMeta meta = (ColumnMeta) obj;
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
	
	public boolean isRequired() {
		return required;
	}

}
