package hr.hrg.hipster.sql;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import hr.hrg.hipster.entity.*;
import hr.hrg.hipster.query.*;
import hr.hrg.hipster.type.*;
import hr.hrg.hipster.visitor.*;

public interface IHipsterConnection {

	Connection getConnection();

	Query getLastQuery();

	HipsterSql getHipster();

	Connection getSqlConnection();
	
	public Query q(Object ...parts);


	/** Get first value from first row and first column. <br>
	 * Useful for counting and other queries that return single value.<br>
	 * 
	 * @param sql varargs query
	 * @return single result Object
	 */
	Object oneObj(Object... sql);
	
	/** 
	 * Get first value as long from first row and first column. <br>
	 * @param sql varargs query
	 * @return single result String
	 */
	String oneString(Object... sql);

	/**
	 *  Get first value as int from first row and first column. <br>
	 * Useful for counting and other queries that return single int value.<br>
	 * @param sql varargs query
	 * @return single result int
	 */
	int oneInt(Object... sql);

	/**
	 *  Get first value as long from first row and first column. <br>
	 * @param sql varargs query
	 * @return single result long
	 */
	long oneLong(Object... sql);

	
	/**
	 *  Get first value as double from first row and first column. <br>
	 * @param sql varargs query
	 * @return single result double
	 */
	double oneDouble(Object ...sql);

	
	/**
	 *  Get first value as parsed by the CustomType from first row and first column. <br>
	 *  @param <T> column value type
	 * @param reader CustomType that reads column
	 * @param sql varargs query
	 * @return single result double
	 */
	<T> T one(ICustomType<T> reader, Object ...sql);
	
	/**
	 * Get single row. 
	 * @param sql varargs query
	 * @return single row
	 */
	Map<Object, Object> row(Object... sql);

	List<Map<Object, Object>> rows(Object... sql);

	<T> T entity(Class<T> clazz, Object... sql);
	<T,ID> T entity(IEntityMeta<T, ID> reader, Object ...sql);

	<T> List<T> entitiesLimit(Class<T> clazz, int offset, int limit, Object... sql);

	<T> List<T> entities(Class<T> clazz, Object... sql);
	
	
	List<Object> column(Object... sql);

	List<Long> columnLong(Object... sql);

	List<Integer> columnInteger(Object... sql);

	List<String> columnString(Object... sql);

	List<Float> columnFloat(Object... sql);

	List<Double> columnDouble(Object... sql);

	Map<Object, Object> treeWithValue(Object... sql);

	Map<Object, Map<Object, Object>> treeWithRow(Query sql, String... columns);

	/** 
	 * Execute update and return number of affected rows 
	 * 
	 * @param sql query
	 * @return number of affected rows
	 */
	int update(Query sql);
	<T,ID> int update(IEntityMeta<T, ID> meta, IUpdatable mutable);

	Object insert(Query sql);
	<T> T insert(Class<T> primaryColumnType, Query sql);
	<T,ID> ID insert(IEntityMeta<T, ID> meta, IUpdatable mutable);

	
	<T,ID> List<T> entities(IEntityMeta<T,ID> reader, Object ...sql);
	<T,ID> List<T> entitiesLimit(IEntityMeta<T,ID> reader, int offset, int limit, Object ...sql);


	<T> List<T> column(Class<T> reader, Object ...sql);
	<T> List<T> column(ICustomType<T> reader, Object ...sql);

	void rowsVisitResult(Query sql, IResultSetVisitor visitor);

	<T1> void rowsVisitResult(
			ColumnMeta<T1> c1, 
			Query filter, 
			Lambda1<T1> visitor);

	<T1, T2> void rowsVisitResult(
			ColumnMeta<T1> c1, 
			ColumnMeta<T2> c2, 
			Query filter, 
			Lambda2<T1, T2> visitor);

	<T1, T2, T3> void rowsVisitResult(
			ColumnMeta<T1> c1, 
			ColumnMeta<T2> c2, 
			ColumnMeta<T3> c3, 
			Query filter,
			Lambda3<T1, T2, T3> visitor);

	<T1, T2, T3, T4> void rowsVisitResult(
			ColumnMeta<T1> c1, 
			ColumnMeta<T2> c2, 
			ColumnMeta<T3> c3, 
			ColumnMeta<T4> c4,
			Query filter, 
			Lambda4<T1, T2, T3, T4> visitor);

	<T1, T2, T3, T4, T5> void rowsVisitResult(
			ColumnMeta<T1> c1,
			ColumnMeta<T2> c2,
			ColumnMeta<T3> c3,
			ColumnMeta<T4> c4, 
			ColumnMeta<T5> c5, 
			Query filter, 
			Lambda5<T1, T2, T3, T4, T5> visitor);

	<T1, T2, T3, T4, T5, T6> void rowsVisitResult(
			ColumnMeta<T1> c1, 
			ColumnMeta<T2> c2, 
			ColumnMeta<T3> c3,
			ColumnMeta<T4> c4, 
			ColumnMeta<T5> c5, 
			ColumnMeta<T6> c6,
			Query filter,
			Lambda6<T1, T2, T3, T4, T5, T6> visitor);

	<T1, T2, T3, T4, T5, T6, T7> void rowsVisitResult(
			ColumnMeta<T1> c1,
			ColumnMeta<T2> c2,
			ColumnMeta<T3> c3,
			ColumnMeta<T4> c4,
			ColumnMeta<T5> c5,
			ColumnMeta<T6> c6, 
			ColumnMeta<T7> c7, 
			Query filter,
			Lambda7<T1, T2, T3, T4, T5, T6, T7> visitor);

	<T1, T2, T3, T4, T5, T6, T7, T8> void rowsVisitResult(
			ColumnMeta<T1> c1,
			ColumnMeta<T2> c2,
			ColumnMeta<T3> c3,
			ColumnMeta<T4> c4,
			ColumnMeta<T5> c5,
			ColumnMeta<T6> c6, 
			ColumnMeta<T7> c7, 
			ColumnMeta<T8> c8,
			Query filter,
			Lambda8<T1, T2, T3, T4, T5, T6, T7, T8> visitor);

	<T1, T2, T3, T4, T5, T6, T7, T8, T9> void rowsVisitResult(
			ColumnMeta<T1> c1,
			ColumnMeta<T2> c2, 
			ColumnMeta<T3> c3,
			ColumnMeta<T4> c4,
			ColumnMeta<T5> c5, 
			ColumnMeta<T6> c6, 
			ColumnMeta<T7> c7,
			ColumnMeta<T8> c8,
			ColumnMeta<T9> c9, 
			Query filter, 
			Lambda9<T1, T2, T3, T4, T5, T6, T7, T8, T9> visitor);

	<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> void rowsVisitResult(
			ColumnMeta<T1> c1, 
			ColumnMeta<T2> c2,
			ColumnMeta<T3> c3,
			ColumnMeta<T4> c4, 
			ColumnMeta<T5> c5,
			ColumnMeta<T6> c6,
			ColumnMeta<T7> c7,
			ColumnMeta<T8> c8,
			ColumnMeta<T9> c9,
			ColumnMeta<T10> c10,
			Query filter,
			Lambda10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> visitor);

	<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> void rowsVisitResult(
			ColumnMeta<T1> c1,
			ColumnMeta<T2> c2,
			ColumnMeta<T3> c3,
			ColumnMeta<T4> c4, 
			ColumnMeta<T5> c5, 
			ColumnMeta<T6> c6, 
			ColumnMeta<T7> c7,
			ColumnMeta<T8> c8, 
			ColumnMeta<T9> c9,
			ColumnMeta<T10> c10,
			ColumnMeta<T11> c11, 
			Query filter,
			Lambda11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> visitor);


	<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> void rowsVisitResult(
			ColumnMeta<T1> c1,
			ColumnMeta<T2> c2,
			ColumnMeta<T3> c3,
			ColumnMeta<T4> c4, 
			ColumnMeta<T5> c5, 
			ColumnMeta<T6> c6, 
			ColumnMeta<T7> c7,
			ColumnMeta<T8> c8, 
			ColumnMeta<T9> c9,
			ColumnMeta<T10> c10,
			ColumnMeta<T11> c11, 
			ColumnMeta<T12> c12, 
			Query filter,
			Lambda12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> visitor);
	
	<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> void rowsVisitResult(
			ColumnMeta<T1> c1,
			ColumnMeta<T2> c2,
			ColumnMeta<T3> c3,
			ColumnMeta<T4> c4, 
			ColumnMeta<T5> c5, 
			ColumnMeta<T6> c6, 
			ColumnMeta<T7> c7,
			ColumnMeta<T8> c8, 
			ColumnMeta<T9> c9,
			ColumnMeta<T10> c10,
			ColumnMeta<T11> c11, 
			ColumnMeta<T12> c12, 
			ColumnMeta<T13> c13, 
			Query filter,
			Lambda13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> visitor);
	
	<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> void rowsVisitResult(
			ColumnMeta<T1> c1,
			ColumnMeta<T2> c2,
			ColumnMeta<T3> c3,
			ColumnMeta<T4> c4, 
			ColumnMeta<T5> c5, 
			ColumnMeta<T6> c6, 
			ColumnMeta<T7> c7,
			ColumnMeta<T8> c8, 
			ColumnMeta<T9> c9,
			ColumnMeta<T10> c10,
			ColumnMeta<T11> c11, 
			ColumnMeta<T12> c12, 
			ColumnMeta<T13> c13, 
			ColumnMeta<T14> c14, 
			Query filter,
			Lambda14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> visitor);
	
	<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> void rowsVisitResult(
			ColumnMeta<T1> c1,
			ColumnMeta<T2> c2,
			ColumnMeta<T3> c3,
			ColumnMeta<T4> c4, 
			ColumnMeta<T5> c5, 
			ColumnMeta<T6> c6, 
			ColumnMeta<T7> c7,
			ColumnMeta<T8> c8, 
			ColumnMeta<T9> c9,
			ColumnMeta<T10> c10,
			ColumnMeta<T11> c11, 
			ColumnMeta<T12> c12, 
			ColumnMeta<T13> c13, 
			ColumnMeta<T14> c14, 
			ColumnMeta<T15> c15, 
			Query filter,
			Lambda15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> visitor);
	
	<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> void rowsVisitResult(
			ColumnMeta<T1> c1,
			ColumnMeta<T2> c2,
			ColumnMeta<T3> c3,
			ColumnMeta<T4> c4, 
			ColumnMeta<T5> c5, 
			ColumnMeta<T6> c6, 
			ColumnMeta<T7> c7,
			ColumnMeta<T8> c8, 
			ColumnMeta<T9> c9,
			ColumnMeta<T10> c10,
			ColumnMeta<T11> c11, 
			ColumnMeta<T12> c12, 
			ColumnMeta<T13> c13, 
			ColumnMeta<T14> c14, 
			ColumnMeta<T15> c15, 
			ColumnMeta<T16> c16, 
			Query filter,
			Lambda16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> visitor);
	

}