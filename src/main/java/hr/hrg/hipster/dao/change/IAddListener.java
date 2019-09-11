package hr.hrg.hipster.dao.change;

import hr.hrg.hipster.dao.*;
import hr.hrg.hipster.sql.*;

public interface IAddListener<T, ID, E extends BaseColumnMeta, M extends IEntityMeta<T, ID, E>> {
	
	public void recordAdded(ID id, T data, M meta, long batchId);
}
