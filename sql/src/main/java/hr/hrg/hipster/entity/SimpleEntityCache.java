package hr.hrg.hipster.entity;

import java.util.*;

import hr.hrg.hipster.change.*;
import hr.hrg.hipster.sql.*;

public class SimpleEntityCache<T, ID, M extends IEntityMeta<T, ID>> 
	implements IEntityEventListener<T, ID, M>{

	protected Map<ID, T> byId = new HashMap<>();
	protected M meta;
	
	public SimpleEntityCache(EntityEventHub entityEventHub, M meta) {
		this.meta = meta;
		entityEventHub.addListenerByMeta(meta,this,
			EntityEventType.AFTER_ADD,
			EntityEventType.AFTER_CHANGE,
			EntityEventType.AFTER_DELETE
		);
	}

	public List<T> load(IHipsterConnection conn, Object ...filter) {
		List<T> list = conn.entities(meta, filter);
		init(list);
		return list;
	}

	public void init(List<T> initial) {
		for (T t : initial) {
			recordAdded(meta.entityGetPrimary(t), t, meta, -1);
		}
	}
	

	@Override
	public void entityEvent(EntityEventType type, 
			ID id, 
			T old, 
			T updated, IUpdatable delta,
			M meta, 
			long batchId) {

		switch(type) {
			case AFTER_ADD: recordAdded(id, updated, (M)meta, batchId); break; 
			case AFTER_CHANGE: recordChanged(id, old, updated, meta, batchId); break; 
			case AFTER_DELETE: recordDeleted(id, old, batchId); break;
			default:;
		}
	}
	
	public void recordAdded(ID id, T data, M meta, long batchId) {
		byId.put(id, data);
	}

	public void recordChanged(ID id, T old, T updated, M meta, long batchId) {
		byId.replace(id, updated);
	}
	
	public void recordDeleted(ID id, T old, long batchId) {
		byId.remove(id);
	}

	public void clear() {
		byId.clear();
	}
	
	public M getMeta() {
		return meta;
	}
	
	public T getById(ID id) {
		if(id == null) return null;
		return byId.get(id);
	}
}
