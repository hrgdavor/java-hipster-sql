package hr.hrg.hipster.sql;

import java.sql.*;

public interface IResultVisitor<T> {
	
	public String getColumnNamesStr();
	
	public void visitResult(ResultSet rs, T fwd) throws SQLException;

}
