package hr.hrg.hipster.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface IPreparedValue{

	public void set(PreparedStatement ps, int index) throws SQLException;
	
}
