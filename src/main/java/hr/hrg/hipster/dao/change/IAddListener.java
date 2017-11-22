package hr.hrg.hipster.dao.change;

import hr.hrg.hipster.dao.*;
import hr.hrg.hipster.sql.*;

public interface IAddListener<T, ID, E extends IColumnMeta> {
	public void recordAdded(ID id, T data, IEntityMeta<T,ID,E> meta, long batchId);
}
