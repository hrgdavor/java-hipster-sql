package hr.hrg.hipster.sql;

import java.sql.*;

public interface IResultSetVisitor {
		
	public void visitResult(ResultSet rs) throws SQLException;

}
