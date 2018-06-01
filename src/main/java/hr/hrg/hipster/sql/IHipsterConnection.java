package hr.hrg.hipster.sql;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface IHipsterConnection {

	Connection getConnection();

	Query getLastQuery();

	PreparedQuery getLastPrepared();

	HipsterSql getHipster();

	Connection getSqlConnection();

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
	int one(Object... sql);

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
	 * Get single row. 
	 * @param sql varargs query
	 * @return single row
	 */
	Map<Object, Object> row(Object... sql);

	List<Map<Object, Object>> rows(Object... sql);

	<T> T entity(Class<T> clazz, Object... sql);

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
	 * @param sql varags query
	 * @return number of affected rows
	 */
	int update(Object ...sql);

	Object insert(Query sql);
	<T> T insert(Class<T> primaryColumnType, Query sql);

	<T,E extends BaseColumnMeta> T entity(IReadMeta<T, E> reader, Object ...sql);
	
	<T,E extends BaseColumnMeta> List<T> entities(IReadMeta<T, E> reader, Object ...sql);
	<T,E extends BaseColumnMeta> List<T> entitiesLimit(IReadMeta<T, E> reader, int offset, int limit, Object ...sql);


	<T> List<T> column(Class<T> reader, Object ...sql);
	<T> List<T> column(ICustomType<T> reader, Object ...sql);

	public <T> void rowsVisit(Object sql, T visitor);
	
	<T> void rowsVisitFwd(Object sql, IResultFwdVisitor<T> visitor, T fwd);

	void rowsVisitResult(Object sql, IResultSetVisitor visitor);

	
}