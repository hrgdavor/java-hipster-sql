package hr.hrg.hipster.dao.test;

import java.sql.*;
import java.util.*;

import hr.hrg.hipster.sql.*;
import hr.hrg.hipster.type.*;

class StringListGetter implements ICustomType<List<String>>{

	@Override
	public List<String> get(ResultSet rs, int index) throws SQLException {
		String string = rs.getString(index);
		if(string == null) return Collections.emptyList();
		string = string.trim();
		if(string.isEmpty()) return Collections.emptyList();
		return ImmutableList.safe(string.split(","));
	}

	@Override
	public void set(PreparedStatement ps, int index, List<String> value) throws SQLException {
		if(value == null) ps.setNull(index, Types.VARCHAR);
		StringBuffer b = new StringBuffer();
		for(int i=0; i<value.size(); i++) {
			if(i>0) b.append(",");
			b.append(value.get(i));
		}
		ps.setString(index, b.toString());
	}		
}