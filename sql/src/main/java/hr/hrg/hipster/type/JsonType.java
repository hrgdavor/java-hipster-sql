package hr.hrg.hipster.type;

import java.io.*;
import java.sql.*;

import com.fasterxml.jackson.core.type.*;
import com.fasterxml.jackson.databind.*;

public class JsonType<T> implements ICustomType<T>{

	private String clazz;
	private ObjectReader reader;
	private ObjectWriter writer;
	private JavaType javaType;

	public JsonType(ObjectMapper mapper, JavaType javaType) {
		this.javaType = javaType;
		reader = mapper.readerFor(javaType);
		writer = mapper.writerFor(javaType);
		mapper.getTypeFactory();
		this.clazz = javaType.getTypeName();
	}

	public JsonType(ObjectMapper mapper, TypeReference<T> typeRef) {
		reader = mapper.readerFor(typeRef);
		writer = mapper.writerFor(typeRef);
		this.clazz = typeRef.getType().getTypeName();
	}

	public JsonType(ObjectMapper mapper, Class<T> clazz) {
		reader = mapper.readerFor(clazz);
		writer = mapper.writerFor(clazz);
		this.clazz = clazz.getName();
	}
	
	@Override
	public T get(ResultSet rs, int index) throws SQLException {
		InputStream src = rs.getBinaryStream(index);
		if(src == null) return null;
		try {
			return reader.readValue(src);
		}catch (Exception e) {
			throw new SQLException("Problem reading result from index "+index+" to convert to "+clazz,e);
		}
	}

	@Override
	public void set(PreparedStatement ps, int index, T value) throws SQLException {
		if(value == null) {
			ps.setNull(index, Types.VARCHAR);
		}else {
			try {
				ps.setString(index, writer.writeValueAsString(value));
			}catch (Exception e) {
				throw new SQLException("Problem writing result to index "+index+" converted from "+clazz,e);
			}
		}
	}

	public JavaType getJavaType() {
		return javaType;
	}

	public ObjectReader getReader() {
		return reader;
	}

	public ObjectWriter getWriter() {
		return writer;
	}
}
