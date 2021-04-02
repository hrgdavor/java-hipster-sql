package hr.hrg.hipster.dao.change;

import hr.hrg.hipster.dao.*;
import hr.hrg.hipster.sql.*;

public class EntityEvent<T, ID, E extends BaseColumnMeta>{

	public enum Type{
		BEFORE_ADD,	   AFTER_ADD,
		BEFORE_CHANGE, AFTER_CHANGE,
		BEFORE_DELETE, AFTER_DELETE
	}
	
	private ID id;
	private T old;
	private T updated;
	private IUpdatable delta;
	private IEntityMeta<T,ID,E> meta;

	public EntityEvent(ID id, T old, T updated, IUpdatable delta, IEntityMeta<T,ID,E> meta){
		this.id = id;
		this.old = old;
		this.updated = updated;
		this.delta = delta;
		this.meta = meta;
	}
	
	public T getOld() {
		return old;
	}
	
	public T getUpdated() {
		return updated;
	}
	
	public IUpdatable getDelta() {
		return delta;
	}
	
	public ID getId() {
		return id;
	}
	
	public IEntityMeta<T,ID,E> getMeta() {
		return meta;
	}

	public boolean isNew() {
		return old == null;
	}
}