package hr.hrg.hipster.dao;

import hr.hrg.hipster.sql.*;

public interface IEnumGetter {
	/** Type safe version for coded usage.
	 * 
	 * @param column Enum for column
	 * @return Object value
	 */	
	public<T,E extends Key<T>> T getValue(E column);

	/** Index based version for looping.
	 * 
	 * @param ordinal column index
	 * @return Object value
	 * 
	 */	
	public Object getValue(int ordinal);

}
