package hr.hrg.hipstersql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedValue{

	public void set(PreparedStatement ps, int index) throws SQLException;
	
}
