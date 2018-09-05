package hr.hrg.hipster.sql;

import hr.hrg.hipster.dao.*;

@SuppressWarnings("rawtypes")
public class EntityQuery <T,ID,C extends BaseColumnMeta> extends Query{

	private IEntityMeta<T, ID, C> meta;

	public EntityQuery(IEntityMeta<T, ID, C> meta) {
		this.meta = meta;
	}
	
	public EntityQuery<T, ID, C> addColumnValue(BaseColumnMeta column, Object value) {
		append(meta.getTypeHandler(column.ordinal()), value);
		return this;
	}

	public EntityQuery<T, ID, C> addColumnOpValue(BaseColumnMeta column, String op, Object value) {
		if(op.charAt(0) != ' ') append(Query.QUERY_SPACE);
		append(column, op, meta.getTypeHandler(column.ordinal()), value);
		return this;
	}

	public EntityQuery<T, ID, C> addColumnOpValue(BaseColumnMeta column, String op, Object value, String qAfter) {
		if(op.charAt(0) != ' ') append(Query.QUERY_SPACE);
		append(column, op, meta.getTypeHandler(column.ordinal()), value, qAfter);
		return this;
	}

	public EntityQuery<T, ID, C> addColumnInValue(BaseColumnMeta column, String op, Object ...values){
		if(op.charAt(0) != ' ') append(Query.QUERY_SPACE);
		append(column, op);
		ICustomType<?> typeHandler = meta.getTypeHandler(column.ordinal());
		
		for(int i=0; i<values.length; i++) {
			if(i ==0){
				append("(", typeHandler, values[i]);
			}else {
				append(",", typeHandler, values[i]);
			}
		}
		append(")");		
		return this;
	}

	@Override
	public EntityQuery<T, ID, C> append(Object... rightSide) {
		super.append(rightSide);
		return this;
	}

	@Override
	public EntityQuery<T, ID, C> appendValue(Object value) {
		super.appendValue(value);
		return this;
	}

	/**
	 * Alias to append
	 * @param rightSide query
	 * @return self
	 */
	public EntityQuery<T, ID, C> add(Object... rightSide) {
		super.append(rightSide);
		return this;
	}

	/**
	 * Alias to appendValue
	 * 
	 * @param value value
	 * @return self
	 */
	public EntityQuery<T, ID, C> addValue(Object value) {
		super.appendValue(value);
		return this;
	}
	
}
