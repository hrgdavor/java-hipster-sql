package hr.hrg.hipster.dao.change;

import hr.hrg.hipster.sql.*;

public interface IDeletedListener<ID, E extends BaseColumnMeta> {

	public void recordDeleted(ID id, long batchId);	
}
