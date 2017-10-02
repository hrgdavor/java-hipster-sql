package hr.hrg.hipster.sql;

import java.util.*;

public interface IColumnMeta extends IQueryLiteral{
	/** 
	 * @return column name in database
	 */
	public String getColumnName();
	
	/** 
	 * @return name in Enum and the name of the field in the Entity class
	 */
	public String name();
	
	/** 
	 * @return type of the field
	 */
	public Class<?> getType();
	
	/** 
	 * @return if the value is primitive type (int,long...etc) in the implemented entity class.
	 */
	public boolean isPrimitive();
	
	public int ordinal();

	boolean isGeneric();

	List<Class<?>> getTypeParams();

	public Class<?> getEntity();
	
	public String getTableName();
}
