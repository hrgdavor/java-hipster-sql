package hr.hrg.hipster.dao.change;

import hr.hrg.hipster.dao.*;
import hr.hrg.hipster.sql.*;

public interface IDeletedDetailListener<T, ID, E extends IColumnMeta> {

	public void recordDeletedDetails(ID id, T old, IEntityMeta<T,ID,E> meta, long batchId);	
}
