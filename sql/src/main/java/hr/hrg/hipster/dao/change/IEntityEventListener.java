package hr.hrg.hipster.dao.change;

import hr.hrg.hipster.dao.*;

@SuppressWarnings("rawtypes")
public interface IEntityEventListener<T, ID, M extends IEntityMeta<T, ID>>{
	
	public void entityEvent(
			EntityEventType type, 
			ID id, 
			T old, 
			T updated, 
			IUpdatable delta, 
			M meta,
			long batchId);
}
