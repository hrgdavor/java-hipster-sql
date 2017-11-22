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
}
