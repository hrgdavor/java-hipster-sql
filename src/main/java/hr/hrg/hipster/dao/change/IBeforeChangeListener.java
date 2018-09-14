package hr.hrg.hipster.dao.change;

import hr.hrg.hipster.dao.*;
import hr.hrg.hipster.sql.*;

public interface IBeforeChangeListener<T, ID, E extends BaseColumnMeta, U extends IUpdatable> {

	public void recordWillChange(ID id, T old, U update, IEntityMeta<T,ID,E> meta, long batchId);
}
