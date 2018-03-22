package hr.hrg.hipster.dao;

import java.util.*;

import hr.hrg.hipster.sql.*;

public class EntityDao <T, ID>{

	protected IHipsterConnection conn;
	protected IEntityMeta<T, ID, ? extends IColumnMeta> meta;
	protected String byIdQuery;
	protected String selectQuery;

	@SuppressWarnings("unchecked")
	public EntityDao(Class<T> clazz, IHipsterConnection conn, EntitySource entitySource){
		this.meta = (IEntityMeta<T, ID, ? extends IColumnMeta>) entitySource.getFor(clazz);
		this.conn = conn;
		init();
	}

	public EntityDao(IEntityMeta<T, ID, ? extends IColumnMeta> meta, IHipsterConnection conn){
		this.meta = meta;
		this.conn = conn;
		init();
	}

	public void init() {
		selectQuery = "select "+meta.getColumnNamesStr()+" FROM "+meta.getTableName()+" ";

		if(meta.getPrimaryColumn() != null){
			this.byIdQuery = selectQuery+"WHERE "+meta.getPrimaryColumn().getColumnName()+"=?";
		}
	}

	public IHipsterConnection getConnection() {
		return conn;
	}
	
	public IEntityMeta<T, ID, ? extends IColumnMeta> getMeta() {
		return meta;
	}
	
	public T byId(ID id){
		if(byIdQuery == null) throw new NullPointerException("Entity "+meta.getEntityClass().getName()+" does not have a primary column defined");
		
		try(Result res = new Result(conn);){
		
			res.executePrepared(byIdQuery, id);
        	
			return res.fetchEntity(meta);
		}		
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
	Object oneObj(IColumnMeta column, Object... sql) {
		return conn.oneObj(
				"SELECT ", column,
				" FROM "+meta.getTableName(), 
				q(sql));
		
	}

	/** 
	 * Get first value as long from first row and first column. <br>
	 * @param sql varargs query
	 * @return single result String
	 */
	public String oneString(IColumnMeta column, Object... sql) {
		return conn.oneString(
				"SELECT ", column,
				" FROM "+meta.getTableName(), 
				q(sql));
		
	}

	/**
	 *  Get first value as int from first row and first column. <br>
	 * Useful for counting and other queries that return single int value.<br>
	 * @param sql varargs query
	 * @return single result int
	 */
	int one(IColumnMeta column, Object... sql) {
		return conn.one(
				"SELECT ", column,
				" FROM "+meta.getTableName(), 
				q(sql));
		
	}


	/**
	 *  Get first value as long from first row and first column. <br>
	 * @param sql varargs query
	 * @return single result long
	 */
	long oneLong(IColumnMeta column, Object... sql) {
		return conn.oneLong(
				"SELECT ", column,
				" FROM "+meta.getTableName(), 
				q(sql));
		
	}
	
	/**
	 *  Get first value as double from first row and first column. <br>
	 * @param sql varargs query
	 * @return single result double
	 */
	double oneDouble(IColumnMeta column, Object... sql) {
		return conn.oneDouble(
				"SELECT ", column,
				" FROM "+meta.getTableName(), 
				q(sql));
		
	}


}
