package hr.hrg.hipster.sql;


import static org.testng.Assert.*;

import java.sql.*;

import org.testng.annotations.*;

import hr.hrg.hipster.dao.*;
import hr.hrg.hipster.query.*;

@Test
@SuppressWarnings("rawtypes")
public class TestQueryRepeat {

	static HipsterSql hip = new HipsterSql();

	EntityMetaSimple<Object, Long, ColumnMeta,Object> meta = new EntityMetaSimple<>(0, "users", Object.class);
	ColumnMeta<Long> idColumn = new ColumnMeta<>(0, "id", "userId", "getId", meta, Long.class, null, "", 
			hip.getTypeSource().getFor(Long.class));

	volatile Query query = new Query(hip,1);// throw-away instance to 
	
	@Test
	public void testThreadSafe() {
		
		Query tmp = query;
		threadSafe(1L);
		assertNotSame(query, tmp, "must NOT be same ");
		
		tmp = query;
		threadSafe(2L);
		assertNotSame(query, tmp, "must NOT be same "); // cloned, can not be same object
		
		tmp = query;
		threadSafe(3L);
		assertNotSame(query, tmp, "must NOT be same ");
		
	}
	
	void threadSafe(Long id) {
		
		query = query.clone() // thread safe version clones original query (all fields are reused except values array)
//		query2 = query2.init() // not thread safe, reuses same QueryRepeat
				.add("SELECT * FROM ", meta)
				.add(" WHERE ")
				.add(idColumn,"=",id)
				.toRepeatable();// although size is reset, array with values is untouched and is safe to perform the query using this instance
		
		assertEquals(query.getQueryExpression(), "SELECT * FROM \"users\" WHERE \"userId\"=?");
		assertEquals(query.toString(), "SELECT * FROM \"users\" WHERE \"userId\"="+id);
		
	}
		
	@Test
	public void testPreaperd() {
		QueryRepeat query = hip.q("SELECT * FROM ", meta, " WHERE ").add(idColumn,"=", 0).toRepeatable();
		
		assertEquals(query.getQueryExpression(), "SELECT * FROM \"users\" WHERE \"userId\"=?");
		assertEquals(query.toString(), "SELECT * FROM \"users\" WHERE \"userId\"=0");

		query = query.clone().withValues(1);
		
		assertEquals(query.getQueryExpression(), "SELECT * FROM \"users\" WHERE \"userId\"=?");
		assertEquals(query.toString(), "SELECT * FROM \"users\" WHERE \"userId\"=1");
	}
	

	public static class EntityMetaSimple<T, ID, C extends ColumnMeta, V> extends EntityMeta<T, ID, C, V> {

		private String entityName;

		public EntityMetaSimple(int ordinal, String tableName, Class<T> entityClass) {
			super(ordinal, tableName, entityClass);
			this.entityName = entityClass.getSimpleName();
		}

		@Override
		public String getEntityName() {
			return entityName;
		}

		@Override
		public boolean containsColumn(String columnName) {
			return false;
		}

		@Override
		public ColumnMeta<ID> getPrimaryColumn() {
			return null;
		}

		@Override
		public ID entityGetPrimary(T instance) {
			return null;
		}

		@Override
		public IUpdatable mutableCopy(Object v) {
			return null;
		}

		@Override
		public T fromResultSet(ResultSet rs) throws SQLException {
			return null;
		}

	}
	
}
