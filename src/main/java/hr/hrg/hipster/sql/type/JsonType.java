package hr.hrg.hipster.sql.type;

import java.io.*;
import java.sql.*;

import com.fasterxml.jackson.databind.*;

import hr.hrg.hipster.sql.*;

public class JsonType<T> implements ICustomType<T>{

	private ObjectMapper mapper;
	private Class<T> clazz;

	public JsonType(ObjectMapper mapper, Class<T> clazz) {
		this.mapper = mapper;
		this.clazz = clazz;
	}
	
	@Override
	public T get(ResultSet rs, int index) throws SQLException {
		InputStream in = rs.getBinaryStream(index);
		if(in == null) return null;
		try {
			return mapper.readValue(in, clazz);
		}catch (Exception e) {
			throw new SQLException("Problem reading result from index "+index+" to convert to "+clazz.getName(),e);
		}
	}

	@Override
	public void set(PreparedStatement ps, int index, T value) throws SQLException {
		if(value == null) {
			ps.setNull(index, Types.VARCHAR);
		}else {
			try {
				ps.setString(index, mapper.writeValueAsString(value));
			}catch (Exception e) {
				throw new SQLException("Problem writing result to index "+index+" converted from "+clazz.getName(),e);
			}
		}
	}

}
