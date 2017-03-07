package hr.hrg.hipstersql.setter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedSetter<T> {

	public void set(PreparedStatement ps, int index, T value) throws SQLException;
	
}
