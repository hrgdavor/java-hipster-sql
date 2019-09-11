package hr.hrg.hipster.dao.change;

import hr.hrg.hipster.sql.*;

public interface IChangeListener<T, ID, E extends BaseColumnMeta> {

	public void recordChanged(EntityEvent<T,ID,E> update, long batchId);
}
