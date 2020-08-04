package hr.hrg.hipster.entity;

import java.lang.reflect.*;
import java.util.*;

import org.bson.*;
import org.bson.codecs.*;
import org.bson.codecs.configuration.*;

import hr.hrg.hipster.sql.*;

public abstract class MongoEntityMeta<T,ID, C extends ColumnMeta, V> extends EntityMeta<T, ID, C, V> implements Codec<T> {

	private CodecRegistry _codecRegistry;
	protected Codec<?>[] _codecs;
	
	public MongoEntityMeta(int ordinal, String tableName, Class<T> entityClass, HipsterSql hipster) {
		super(ordinal, tableName, entityClass, hipster);
	}

	public MongoEntityMeta(int ordinal, String tableName, Class<T> entityClass, C[] columnArray, String[] columnArraySortedStr, C[] columnArraySorted, String[] fieldArraySortedStr, C[] fieldArraySorted, HipsterSql hipster) {
		super(ordinal, tableName, entityClass, columnArray, columnArraySortedStr, columnArraySorted, fieldArraySortedStr, fieldArraySorted, hipster);
	}
	
	@Override
	public Class getEncoderClass() {
		
		return _entityClass;
	}

	public CodecRegistry getCodecRegistry() {
		return _codecRegistry;
	}

	public void setCodecRegistry(CodecRegistry _registry) {
		this._codecRegistry = _registry;	
	}

}
