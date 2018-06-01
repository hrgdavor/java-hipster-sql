package hr.hrg.hipster.dao;

import hr.hrg.hipster.sql.*;

public interface IEnumGetter<E extends BaseColumnMeta> {
	/** Type safe version for coded usage.
	 * 
	 * @param column Enum for column
	 * @return Object value
	 */	
	public Object getValue(E column);

	/** Index based version for looping.
	 * 
	 * @param ordinal column index
	 * @return Object value
	 * 
	 */	
	public Object getValue(int ordinal);

}
