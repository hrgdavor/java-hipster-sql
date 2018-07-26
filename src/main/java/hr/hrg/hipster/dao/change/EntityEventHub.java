package hr.hrg.hipster.dao.change;

import java.util.*;

import org.slf4j.*;

import hr.hrg.hipster.dao.*;
import hr.hrg.hipster.sql.*;

@SuppressWarnings({"rawtypes","unchecked"})
public class EntityEventHub {
	
	Logger log = LoggerFactory.getLogger(EntityEventHub.class);
	
	Map<Class, List<IChangeListener>> changeListeners = new HashMap<>();
	Map<Class, List<IBeforeChangeListener>> beforeChangeListeners = new HashMap<>();
	Map<Class, List<IDeletedListener>> deleteListeners = new HashMap<>();
	Map<Class, List<IDeletedDetailListener>> deleteDataListeners = new HashMap<>();
	Map<Class, List<IAddListener>> addListeners = new HashMap<>();

	public <T, ID, E extends BaseColumnMeta> void addChangeListener(IChangeListener<T, ID, E> listener, Class<T> clazz){
		synchronized (clazz) {
			List<IChangeListener> list = changeListeners.get(clazz);
			if(list == null) {
				list = new ArrayList<>();
				changeListeners.put(clazz, list);
			}
			list.add(listener);
		}
	}

	public boolean hasChangeListener(Class<?> clazz){
		return changeListeners.containsKey(clazz);
	}
	
	public <T, ID, E extends BaseColumnMeta, U extends IUpdatable<E>> void addBeforeChangeListener(IBeforeChangeListener<T, ID, E, U> listener, Class<T> clazz){
		synchronized (clazz) {
			List<IBeforeChangeListener> list = beforeChangeListeners.get(clazz);
			if(list == null) {
				list = new ArrayList<>();
				beforeChangeListeners.put(clazz, list);
			}
			list.add(listener);
		}
	}

	public boolean hasBeforeChangeListener(Class<?> clazz){
		return beforeChangeListeners.containsKey(clazz);
	}

	public <T, ID, E extends BaseColumnMeta> void addDeleteListener(IDeletedListener<ID, E> listener, Class<T> clazz){
		synchronized (clazz) {
			List<IDeletedListener> list = deleteListeners.get(clazz);
			if(list == null) {
				list = new ArrayList<>();
				deleteListeners.put(clazz, list);
			}
			list.add(listener);
		}
	}
	
	public boolean hasDeleteListener(Class<?> clazz){
		return deleteListeners.containsKey(clazz);
	}
	
	public <ID, O, E extends BaseColumnMeta> void addDeleteDataListener(IDeletedDetailListener<ID, O, E> listener, Class<O> clazz){
		synchronized (clazz) {
			List<IDeletedDetailListener> list = deleteDataListeners.get(clazz);
			if(list == null) {
				list = new ArrayList<>();
				deleteDataListeners.put(clazz, list);
			}
			list.add(listener);
		}
	}

	public boolean hasDeleteDataListener(Class<?> clazz){
		return deleteDataListeners.containsKey(clazz);
	}
	
	public <T, ID, E extends BaseColumnMeta, M extends IEntityMeta<T, ID, E>> void addAddListener(IAddListener<T, ID, E, M> listener, Class<T> clazz){
		synchronized (clazz) {
			List<IAddListener> list = addListeners.get(clazz);
			if(list == null) {
				list = new ArrayList<>();
				addListeners.put(clazz, list);
			}
			list.add(listener);
		}
	}

	public boolean hasAddListener(Class<?> clazz){
		return addListeners.containsKey(clazz);
	}
		

	public void fireChange(EntityEvent<?,?,?> update, long batchId){
		List<IChangeListener> list = changeListeners.get(update.getMeta().getEntityClass());
		if(list != null){
			for (IChangeListener listener : list) {
				try {
					listener.recordChanged(update, batchId);
				} catch (Throwable e) {
					log.error("Error notifying a listener: "+e.getMessage(),e);
				}
			}
		}
	}

	public void fireBeforeChange(Object id, Object old, IUpdatable<?> update, IEntityMeta<?,?,?> meta, long batchId){
		if(meta.getPrimaryColumn() != null) {
			checkType(id, meta.getPrimaryColumn().getType(), "primary for ", meta.getEntityName());
		}
		checkType(old, meta.getEntityClass(),"");
		checkType(update, meta.getEntityClass(),"");
		
		List<IBeforeChangeListener> list = beforeChangeListeners.get(meta.getEntityClass());

		if(list != null){
			for (IBeforeChangeListener listener : list) {
				try {
					listener.recordWillChange(id, old, update, meta, batchId);
				} catch (Throwable e) {
					log.error("Error notifying a listener: "+e.getMessage(),e);
				}
			}
		}
	}

	public static void checkType(Object o, Class<?> type, String message, Object ...args) {
		if(o == null) return;
		if(!type.isInstance(o)) throw new ClassCastException(String.format(message, args)+" Expected "+type.getName()+" but object is instance of "+o.getClass().getName());
	}

	public void fireDelete(Object id, IEntityMeta<?, ?, ?> meta, long batchId){
		List<IDeletedListener> list = deleteListeners.get(meta.getEntityClass());
		if(list != null){
			for (IDeletedListener listener : list) {
				try {
					listener.recordDeleted(id, batchId);
				} catch (Throwable e) {
					log.error("Error notifying a listener: "+e.getMessage(),e);
				}
			}
		}
	}
	
	public void fireDeleteData(Object id, Object old, IEntityMeta<?,?,?> meta, long batchId){
		List<IDeletedDetailListener> list = deleteDataListeners.get(meta.getEntityClass());
		if(list != null){
			for (IDeletedDetailListener listener : list) {
				try {
					listener.recordDeletedDetails(id, old, meta, batchId);
				} catch (Throwable e) {
					log.error("Error notifying a listener: "+e.getMessage(), e);
				}
			}
		}
	}

	public void fireAdded(Object id, Object obj, IEntityMeta<?,?,?> meta, long batchId){
		List<IAddListener> list = addListeners.get(meta.getEntityClass());
		if(list != null){
			for (IAddListener listener : list) {
				try {
					listener.recordAdded(id, obj, meta, batchId);
				} catch (Throwable e) {
					log.error("Error notifying a listener: "+e.getMessage(),e);
				}
			}
		}
	}
	
}
