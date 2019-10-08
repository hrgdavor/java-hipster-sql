package hr.hrg.hipster.sql;

import java.sql.*;

public interface IResultSetVisitor {

	void visitResult(ResultSet resultSet) throws SQLException;

}
