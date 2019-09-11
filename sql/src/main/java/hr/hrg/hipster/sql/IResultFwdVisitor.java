package hr.hrg.hipster.sql;

import java.sql.*;

public interface IResultFwdVisitor<T> {
	
	public String getColumnNamesStr();
	
	public void visitResult(ResultSet rs, T fwd) throws SQLException;

}
