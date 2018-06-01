package hr.hrg.hipster.dao;

import java.util.*;

import hr.hrg.hipster.sql.*;

public class EntityDao <T, ID>{

	protected IHipsterConnection conn;
	protected IEntityMeta<T, ID, ? extends BaseColumnMeta> meta;
	protected Query.Immutable selectQuery;

	@SuppressWarnings("unchecked")
	public EntityDao(Class<T> clazz, IHipsterConnection conn, EntitySource entitySource){
		this.meta = (IEntityMeta<T, ID, ? extends BaseColumnMeta>) entitySource.getFor(clazz);
		this.conn = conn;
		init();
	}

	public EntityDao(IEntityMeta<T, ID, ? extends BaseColumnMeta> meta, IHipsterConnection conn){
		this.meta = meta;
		this.conn = conn;
		init();
	}

	public void init() {
		
		int columnCount = meta.getColumnCount()*2-1;
		List<? extends BaseColumnMeta> columns = meta.getColumns();
		Object[] tmp = new Object[columnCount+4];
		
		// SELECT
		tmp[0] = "SELECT ";
		// all columns
		int column = 0;
		for(int i=1; i<=columnCount; i++) {
			if(i % 2 == 0) 
				tmp[i]=",";
			else 
				tmp[i] = columns.get(column++);
		}
		// from
		tmp[columnCount+1] = " FROM ";
		// table
		tmp[columnCount+2] = meta.getTable();
		tmp[columnCount+3] = " ";
		
		selectQuery = new Query.Immutable(ImmutableList.safe(tmp));

	}

	public IHipsterConnection getConnection() {
		return conn;
	}
	
	public IEntityMeta<T, ID, ? extends BaseColumnMeta> getMeta() {
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

	public Query q(Object... sql) {
		return new Query(sql);
	}
	
	/** Get first value from first row and first column. <br>
	 * Useful for counting and other queries that return single value.<br>
	 * 
	 * @param sql varargs query
	 * @return single result Object
	 */
	Object oneObj(BaseColumnMeta column, Object... sql) {
		return conn.oneObj(
				"SELECT ", column,
				" FROM ",meta.getTable(), 
				q(sql));
		
	}

	/** 
	 * Get first value as long from first row and first column. <br>
	 * @param column column
	 * @param sql varargs query
	 * @return single result String
	 */
	public String oneString(BaseColumnMeta column, Object... sql) {
		return conn.oneString(
				"SELECT ", column,
				" FROM ",meta.getTable(), 
				q(sql));
		
	}

	/**
	 *  Get first value as int from first row and first column. <br>
	 * Useful for counting and other queries that return single int value.<br>
	 * @param column column
	 * @param sql varargs query
	 * @return single result int
	 */
	int one(BaseColumnMeta column, Object... sql) {
		return conn.one(
				"SELECT ", column,
				" FROM ",meta.getTable(), 
				q(sql));
		
	}


	/**
	 *  Get first value as long from first row and first column. <br>
	 * @param column column
	 * @param sql varargs query
	 * @return single result long
	 */
	long oneLong(BaseColumnMeta column, Object... sql) {
		return conn.oneLong(
				"SELECT ", column,
				" FROM ",meta.getTable(), 
				q(sql));
		
	}
	
	/**
	 *  Get first value as double from first row and first column. <br>
	 * @param column column
	 * @param sql varargs query
	 * @return single result double
	 */
	double oneDouble(BaseColumnMeta column, Object... sql) {
		return conn.oneDouble(
				"SELECT ", column,
				" FROM ",meta.getTable(), 
				q(sql));
		
	}


}
