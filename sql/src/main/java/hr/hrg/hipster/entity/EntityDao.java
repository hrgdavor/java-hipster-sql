package hr.hrg.hipster.entity;

import java.util.*;

import hr.hrg.hipster.query.*;
import hr.hrg.hipster.sql.*;
import hr.hrg.hipster.type.*;

@SuppressWarnings("rawtypes")
public class EntityDao <T, ID, C extends ColumnMeta, M extends IEntityMeta<T, ID>>{

	protected IHipsterConnection hc;
	protected HipsterSql hip;
	protected M meta;
	protected Query selectQuery;
	protected QueryRepeat selectQueryById;

	@SuppressWarnings("unchecked")
	public EntityDao(Class<T> clazz, IHipsterConnection conn){
		this.meta = (M) conn.getHipster().getEntitySource().getFor(clazz);
		this.hc = conn;
		this.hip=conn.getHipster();
		init();
	}

	public EntityDao(M meta, IHipsterConnection conn){
		this.meta = meta;
		this.hc = conn;
		this.hip=conn.getHipster();
		init();
	}

	public void init() {
		selectQuery = HipsterSqlUtil.selectQueryForEntity(hip,meta);
		selectQueryById = selectQuery.clone()
				.add("WHERE ")
				.add(meta.getPrimaryColumn(),"=",0L)
				.toRepeatable();
	}

	/** 
	 * 
	 * @return
	 */
	public IHipsterConnection getConnection() {
		return hc;
	}
	
	/** Metadata for the entity handled by this DAO
	 * 
	 * @return metadata object
	 */
	public M getMeta() {
		return meta;
	}
	
	/** Get one entity (row) from database using primary key (id).
	 * 
	 * @param id value of primary key
	 * @return entity
	 */
	public T qOneById(ID id){
		if(meta.getPrimaryColumn() == null) throw new NullPointerException("Entity "+meta.getEntityClass().getName()+" does not have a primary column defined");
		return hc.entity(meta, selectQueryById.clone().withValue(id));		
	}

	/** Get one entity (row) from database using supplied filter
	 * 
	 * @param queryParts filter starting with WHERE 
	 * @return entity
	 */
	public T qOne(Object ...queryParts){
		return hc.entity(meta, selectQuery.clone().addParts(queryParts));
	}

	/** Get all entities (rows) from database using supplied filter
	 * 
	 * @param queryParts filter starting with WHERE 
	 * @return list of entities
	 */
	public List<T> qAll(Object ...queryParts){
		return hc.entities(meta, selectQuery.clone().addParts(queryParts));		
	}

	/** Create new EntityQuery
	 * 
	 * @param queryParts query parts
	 * @return self
	 */
	public Query q(Object... queryParts) {
		return hip.q(queryParts);
	}

	/**
	 *  Get first value from first row and first column. <br>
	 * Useful for counting and other queries that return single int value.<br>
	 * @param column column
	 * @param sql varargs query
	 * @param <T2> entity tape
	 * @param <C2> column meta
	 * @return single result
	 */
	public <T2, C2 extends ColumnMeta<T2>> T2 qOneValue(C2 column, Object... sql) {
		return _one(column, null, sql);
	}

	/**
	 *  Get first value from first row and first column, but wrapped in an operation. <br>
	 * Useful for counting MAX/MIN and other queries that return single aggregate value or additional function call.<br>
	 * @param op operation to perform on the column
	 * @param column column
	 * @param sql varargs query
	 * @param <T2> entity tape
	 * @param <C2> column meta
	 * @return single result
	 */
	public <T2, C2 extends ColumnMeta<T2>> T2 qOneValue(String op, C2 column, Object... sql) {
		return _one(column, op, sql);
	}
	
	@SuppressWarnings({"unchecked"})
	private <T2, C2 extends ColumnMeta<T2>> T2 _one(C2 column, String op, Object... sql) {
		Query q = q("SELECT ");
		if(op != null && !op.isEmpty()) {
			q.add(op+"(", column).add(")");
		}else {
			q.add(column);
		}
		q.add(" FROM ", meta).add(" ");
		q.addParts(sql);
		ICustomType<?> handler = meta.getTypeHandler((C) column);
		
		return (T2) hc.one(handler, q);
	}

	/**
	 *  Get first value as long from first row and first column. <br>
	 * @param column column
	 * @param sql varargs query
	 * @return single result primitive long
	 */
	public int qOneInt(C column, Object... sql) {
		return hc.oneInt(q("SELECT ", column,	" FROM ",meta, " ").addParts(sql));
	}

	/**
	 *  Get first value as long from first row and first column. <br>
	 * @param column column
	 * @param sql varargs query
	 * @return single result primitive long
	 */
	public long qOneLong(C column, Object... sql) {
		return hc.oneLong(q("SELECT ", column,	" FROM ",meta, " ").addParts(sql));
	}
	
	/**
	 *  Get first value as double from first row and first column. <br>
	 * @param column column
	 * @param sql varargs query
	 * @return single result primitive double
	 */
	public double qOneDouble(C column, Object... sql) {
		return hc.oneDouble(q("SELECT ", column,	" FROM ",meta, " ").addParts(sql));
	}

	public ID insert(IUpdatable mutable) {
		Query insertQuery = hc.getHipster().buildInsert(meta, mutable);
		return (ID) hc.insert(meta.getPrimaryColumn().getType(), insertQuery);
	}
	
	
}

