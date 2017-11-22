package hr.hrg.hipster.dao;

import hr.hrg.hipster.sql.*;

public interface IEnumGetter<E extends IColumnMeta> {
	/** Type safe version for coded usage.
	 * 
	 * @param column Enum for column
	 */	
	public Object getValue(E column);

	/** Index based version for looping.
	 * 
	 * @param ordinal column index
	 * 
	 */	
	public Object getValue(int ordinal);

}
