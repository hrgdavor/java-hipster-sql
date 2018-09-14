package hr.hrg.hipster.dao;

import hr.hrg.hipster.sql.*;

public interface IUpdatable extends IEnumGetter{
	/**
	 * Utility Object that represents null value in array, so it can be recognised during a loop.<br>
	 * 
	 */
	public static final Object NULL_VALUE = "hipstersql.null";
	
	public boolean isEmpty();
	
	public <T> boolean isChanged(Key<T> column);

	public boolean isChanged(int ordinal);

	public <T> void setChanged(Key<T> column, boolean changed);

	public void setChanged(int ordinal, boolean changed);

	public void setChanged(boolean change);

	/** Type safe version for coded usage.
	 * 
	 * @param column Enum for column
	 * @param value new value
	 */	
	public<T> void setValue(Key<T> column, T value);

	
	/** Index based version for looping.
	 * 
	 * @param ordinal column index
	 * @param value new value
	 * 
	 */	
	public void setValue(int ordinal, Object value);
	
}
