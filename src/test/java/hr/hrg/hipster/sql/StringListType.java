package hr.hrg.hipster.sql;

import java.sql.*;
import java.util.*;

import hr.hrg.hipster.sql.*;

class StringListType implements ICustomType<List<String>>{
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
		ps.setString(index, implode(value));
	}		

	public static String implode(List<String> list) {
		if(list == null || list.size() == 0) return null;
		StringBuilder b = new StringBuilder();
		int count=list.size();
		for(int i=0; i<count; i++) {
			if(i>0) b.append(',');
			b.append(list.get(i));
		}
		return b.toString();
	}	
}