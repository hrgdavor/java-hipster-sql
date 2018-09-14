package hr.hrg.hipster.dao;

import java.util.*;

import hr.hrg.hipster.sql.*;

@SuppressWarnings("rawtypes")
public class EntityDao <T, ID, C extends BaseColumnMeta, M extends IEntityMeta<T, ID, C>>{

	protected IHipsterConnection conn;
	protected M meta;
	protected Query.ImmutableQuery selectQuery;

	@SuppressWarnings("unchecked")
	public EntityDao(Class<T> clazz, IHipsterConnection conn, EntitySource entitySource){
		this.meta = (M) entitySource.getFor(clazz);
		this.conn = conn;
		init();
	}

	public EntityDao(M meta, IHipsterConnection conn){
		this.meta = meta;
		this.conn = conn;
		init();
	}

	public void init() {
		selectQuery = HipsterSqlUtil.selectQueryForEntity(meta);
	}

	public IHipsterConnection getConnection() {
		return conn;
	}
	
	public M getMeta() {
		return meta;
	}
	
	public T byId(ID id){
		if(meta.getPrimaryColumn() == null) throw new NullPointerException("Entity "+meta.getEntityClass().getName()+" does not have a primary column defined");
		
		return conn.entity(meta, new Query(selectQuery).append("WHERE ",meta.getPrimaryColumn(),"=",id));		
	}

	public T byCriteria(Object ...queryParts){
		return conn.entity(meta, new Query(selectQuery).append(queryParts));		
	}

	public List<T> allByCriteria(Object ...queryParts){
		return conn.entities(meta, new Query(selectQuery).append(queryParts));		
	}

	/** Create new EntityQuery
	 * 
	 * @param sql
	 * @return
	 */
	public EntityQuery<T, ID, C, M> q(Object... sql) {
		return new EntityQuery<T, ID, C, M>(meta).append(sql);
	}

	/**
	 *  Get first value from first row and first column. <br>
	 * Useful for counting and other queries that return single int value.<br>
	 * @param column column
	 * @param sql varargs query
	 * @return single result
	 */
	public <T2, C2 extends BaseColumnMeta<T2>> T2 one(C2 column, Object... sql) {
		return _one(column, null, sql);
	}

	/**
	 *  Get first value from first row and first column, but wrapped in an operation. <br>
	 * Useful for counting MAX/MIN and other queries that return single aggregate value or additional function call.<br>
	 * @param op operation to perform on the column
	 * @param column column
	 * @param sql varargs query
	 * @return single result
	 */
	public <T2, C2 extends BaseColumnMeta<T2>> T2 one(String op, C2 column, Object... sql) {
		return _one(column, op, sql);
	}
	
	@SuppressWarnings({"unchecked"})
	private <T2, C2 extends BaseColumnMeta<T2>> T2 _one(C2 column, String op, Object... sql) {
		EntityQuery<T, ID, C, M> q = q("SELECT ");
		if(op != null && !op.isEmpty()) {
			q.append(op+"(", column,")");
		}else {
			q.append(column);
		}
		q.append(" FROM ", meta.getTable(), " ");
		q.append(sql);
		ICustomType<?> handler = meta.getTypeHandler((C) column);
		
		return (T2) conn.one(handler, q);
	}


	/**
	 *  Get first value as long from first row and first column. <br>
	 * @param column column
	 * @param sql varargs query
	 * @return single result primitive long
	 */
	public int oneInt(C column, Object... sql) {
		return conn.oneInt(q("SELECT ", column,	" FROM ",meta.getTable(), " ").append(sql));
	}

	/**
	 *  Get first value as long from first row and first column. <br>
	 * @param column column
	 * @param sql varargs query
	 * @return single result primitive long
	 */
	public long oneLong(C column, Object... sql) {
		return conn.oneLong(q("SELECT ", column,	" FROM ",meta.getTable(), " ").append(sql));
	}
	
	/**
	 *  Get first value as double from first row and first column. <br>
	 * @param column column
	 * @param sql varargs query
	 * @return single result primitive double
	 */
	public double oneDouble(C column, Object... sql) {
		return conn.oneDouble(q("SELECT ", column,	" FROM ",meta.getTable(), " ").append(sql));
	}
	
}
