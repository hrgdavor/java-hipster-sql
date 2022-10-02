package hr.hrg.hipster.change;

import java.util.*;

import org.slf4j.*;

import hr.hrg.hipster.entity.*;

@SuppressWarnings({"rawtypes","unchecked"})
public class EntityEventHub {
	
	Logger log = LoggerFactory.getLogger(EntityEventHub.class);
	private EntitySource entitySource;

	private volatile List<IEntityEventListener>[][] listeners = new List[128][];
	private volatile List<IEntityEventListener>[] listenersGlobal;
	
	public EntityEventHub(EntitySource entitySource) {
		this.entitySource = entitySource;
		listenersGlobal = makeListenersList();
	}
	
	private List<IEntityEventListener>[] makeListenersList(){
		return toArray(
				new ArrayList<IEntityEventListener>(),new ArrayList<IEntityEventListener>(),
				new ArrayList<IEntityEventListener>(),new ArrayList<IEntityEventListener>(),
				new ArrayList<IEntityEventListener>(),new ArrayList<IEntityEventListener>()
		);
	}

	private static final List<IEntityEventListener>[] toArray(List<IEntityEventListener> ...list) {
		return list;
	}
	
	public boolean hasListener(EntityEventType type, IEntityMeta meta) {
		// check global
		if(listenersGlobal[type.ordinal()].size() > 0) return true;
		
		// check specific if global not present
		List<IEntityEventListener> list = getListeners(type, meta);
		return list == null ? false : list.size() > 0;
	}
	
	public <T, ID, C extends ColumnMeta> List<IEntityEventListener> getListeners(EntityEventType type, IEntityMeta<T, ID> meta) {
		int ordinal = meta.ordinal();
		if(ordinal >= listeners.length) return null;
		
		return listeners[ordinal] == null ? null : listeners[ordinal][type.ordinal()];
	}
	
	public <T, ID, M extends IEntityMeta<T, ID>> void addListener(Class<T> klass, IEntityEventListener<T, ID, M> listener, EntityEventType ...types) {
		addListenerByMeta((M)entitySource.getForRequired(klass), listener, types);
	}

	public void addListenerGlobal(IEntityEventListener<?, ?, ?> listener, EntityEventType ...types) {
		_add(listener, listenersGlobal, types);
	}
	
	public <T, ID, M extends IEntityMeta<T, ID>> void addListenerByMeta(M meta, IEntityEventListener<T, ID, M> listener, EntityEventType ...types) {
		int ordinal = meta.ordinal();
		List<IEntityEventListener>[] perType = null;
		synchronized (listeners) {
			if(ordinal >= listeners.length) {
				int newSize = listeners.length;
				while(ordinal >= newSize) newSize *=2;
				List<IEntityEventListener>[][] tmp = new List[newSize][];
				System.arraycopy(listeners, 0, tmp, 0, listeners.length);
				listeners = tmp;
			}
			perType = listeners[ordinal];
			if(perType == null) {
				perType = listeners[ordinal] = makeListenersList();
			}
		}
		
		_add(listener, perType, types);
	}

	public <T, ID, M extends IEntityMeta<T, ID>> void _add(IEntityEventListener<T, ID, M> listener,
			List<IEntityEventListener>[] perType, EntityEventType... types) {
		if(types.length == 0) {
			for(int i=0; i<perType.length; i++) {
				perType[i].add(listener);
			}
		}else {			
			for(EntityEventType type:types) {
				perType[type.ordinal()].add(listener);
			}
		}
	}

	public <T, ID, M extends IEntityMeta<T, ID>> void fireEvent(EntityEventType type, ID id, T old, T updated, IUpdatable delta, M meta, long batchId) {
		_fire(type, id, old, updated, delta, meta, batchId, listenersGlobal[type.ordinal()]);
		
		List<IEntityEventListener> list = getListeners(type, meta);
		if(list != null) {
			_fire(type, id, old, updated, delta, meta, batchId, list);
		}
	}

	public <M extends IEntityMeta<T, ID>, ID, T> void _fire(
			EntityEventType type, 
			ID id, 
			T old, 
			T updated,	
			IUpdatable delta, 
			M meta, 
			long batchId, 
			List<IEntityEventListener> list) {

		meta.init(updated);
		for(IEntityEventListener<T,ID,M> listener: list) {
			try {
				listener.entityEvent(type, id, old, updated, delta, meta, batchId);
			} catch (Exception e) {
				log.error(e.getMessage(),e);
			}
		}
	}
	
}
