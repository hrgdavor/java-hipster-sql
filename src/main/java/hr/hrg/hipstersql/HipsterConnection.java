package hr.hrg.hipstersql;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface HipsterConnection {

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
	double oneDouble(Object[] sql);

	/**
	 * Get single row. 
	 * @param sql varargs query
	 * @return single row
	 */
	Map<Object, Object> row(Object... sql);

	/**
	 *  return result as rows, but react on Thread.interrupt 
	 * @param sql varargs query
	 * @return row
	 * @throws InterruptedException if thread is interrupted
	 */
	List<Map<Object, Object>> rowsInterruptible(Object... sql) throws InterruptedException;

	List<Map<Object, Object>> rowsLimit(int offset, int limit, Object... sql);

	List<Map<Object, Object>> rows(Object... sql);

	List<Object> column(Object... sql);

	Map<Object, Object> treeWithValue(Object... sql);

	Map<Object, Map<Object, Object>> treeWithRow(Query sql, String... columns);

	/** 
	 * Execute update and return number of affected rows 
	 * 
	 * @param sql varags query
	 * @return number of affected rows
	 */
	int update(Object sql);

	Object insert(Query sql);


}