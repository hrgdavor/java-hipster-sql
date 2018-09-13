package hr.hrg.hipster.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hr.hrg.hipster.dao.IEntityMeta;
import hr.hrg.hipster.dao.IEnumGetter;
import hr.hrg.hipster.dao.change.EntityEvent;
import hr.hrg.hipster.dao.change.EntityEventHub;
import hr.hrg.hipster.dao.change.IAddListener;
import hr.hrg.hipster.dao.change.IChangeListener;
import hr.hrg.hipster.dao.change.IDeletedListener;
import hr.hrg.hipster.sql.BaseColumnMeta;
import hr.hrg.hipster.sql.IHipsterConnection;

@SuppressWarnings("rawtypes")
public class SimpleEntityCache<T extends IEnumGetter<E>, ID, E extends BaseColumnMeta,M extends IEntityMeta<T, ID, E>> 
	implements IChangeListener<T, ID, E>,
	IAddListener<T, ID, E, M>,
	IDeletedListener<ID, E>{

	protected Map<ID, T> byId = new HashMap<>();
	protected M meta;
	
	@SuppressWarnings("unchecked")
	public SimpleEntityCache(EntityEventHub entityEventHub, M meta) {
		this.meta = meta;
		Class entity = meta.getEntityClass();
		entityEventHub.addChangeListener(this, entity);
		entityEventHub.addAddListener(this, entity);
		entityEventHub.addDeleteListener(this, entity);
	}

	public List<T> load(IHipsterConnection conn, Object ...filter) {
		List<T> list = conn.entities(meta, filter);
		init(list);
		return list;
	}

	@SuppressWarnings("unchecked")
	public void init(List<T> initial) {
		int idIndex = meta.getPrimaryColumn().ordinal();
		for (T t : initial) {
			recordAdded((ID) t.getValue(idIndex), t, meta, -1);
		}
	}
	
	@Override
	public void recordAdded(ID id, T data, M meta, long batchId) {
		byId.put(id, data);
	}

	@Override
	public void recordDeleted(ID id, long batchId) {
		byId.remove(id);
	}
	
	@Override
	public void recordChanged(EntityEvent<T, ID, E> event, long batchId) {
		byId.replace(event.getId(), event.getUpdated());
	}

	public M getMeta() {
		return meta;
	}
	
	public T getById(ID id) {
		return byId.get(id);
	}
}
