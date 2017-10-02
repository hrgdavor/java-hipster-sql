package hr.hrg.hipster.sql.getter;

import java.sql.*;

import hr.hrg.hipster.sql.*;

public class DoubleGetter implements IResultGetter<Double>{

	@Override
	public Double get(ResultSet rs, int index) throws SQLException {
		double ret = rs.getDouble(index);
		if(rs.wasNull()) return null;
		return ret;
		
	}

}
