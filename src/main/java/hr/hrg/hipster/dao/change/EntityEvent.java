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
	private IUpdatable<E> delta;
	private IEntityMeta<T,ID,E> meta;

	public EntityEvent(ID id, T old, T updated, IUpdatable<E> delta, IEntityMeta<T,ID,E> meta){
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
	
	public IUpdatable<E> getDelta() {
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
