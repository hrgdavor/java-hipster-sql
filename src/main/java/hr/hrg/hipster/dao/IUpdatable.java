package hr.hrg.hipster.dao;

import hr.hrg.hipster.sql.*;

public interface IUpdatable<E extends BaseColumnMeta> extends IEnumGetter<E>{
	/**
	 * Utility Object that represents null value in array, so it can be recognised during a loop.<br>
	 * 
	 */
	public static final Object NULL_VALUE = "hipstersql.null";
	
	public boolean isEmpty();
	
	public boolean isChanged(E column);

	public boolean isChanged(int ordinal);

	/** Type safe version for coded usage.
	 * 
	 * @param column Enum for column
	 * @param value new value
	 */	
	public void setValue(E column, Object value);

	/** Index based version for looping.
	 * 
	 * @param ordinal column index
	 * @param value new value
	 * 
	 */	
	public void setValue(int ordinal, Object value);
	
}
