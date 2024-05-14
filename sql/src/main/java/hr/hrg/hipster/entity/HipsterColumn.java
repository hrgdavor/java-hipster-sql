package hr.hrg.hipster.entity;

import java.lang.annotation.*;

import hr.hrg.hipster.sql.*;
import hr.hrg.hipster.type.*;

/**Alternative to {@link javax.persistence.Column}
 * 
 * @author hrg
 *
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface HipsterColumn {

	/**
	 * (Optional) The name of the column. Defaults to 
	 * the property or field name.
	 * @return name 
	 */
	String name() default "";

	/** Description
	 * @return description 
	 */
	String descr() default "";
	
	/** Description
	 * @return description 
	 */
	String[] initial() default {};

	/**
     * (Optional) sql expression for the column. If defined the column name 
     * is added as alias after the expression.
     * @return sql 
     */
    String sql() default "";

    /**
     * (Optional) The name of the _table that contains the column. 
     * If absent the column is assumed to be in the primary _table.
     * @return table 
     */
    String table() default "";
    
    
    /**
     * (Optional) The class of ICustomType that converts the column 
     * from database to entity and vice versa
     * @return custom type
     */
    @SuppressWarnings("rawtypes")
	Class<? extends ICustomType> customType() default ICustomType.class;


    /**
     * (Optional) The class of ICustomType that converts the column 
     * from database to entity and vice versa
     * @return custom type
     */
    String customTypeKey() default "";

    /** is the column required  
     * @return custom type
     */
    BooleanEnum required() default BooleanEnum.DEFAULT;
    
    
    /**
     * Should this method be skipped
     * @return if method should be skipped 
     */
    boolean skip() default false;
    
    /**
     * This property will hold the rest of the properties in case of there are more props
     * to de-serialise than fields available. Also props from this will be expanded into the top
     * level when serialising
     * @return used for rest of props 
     */
    boolean keepRest() default false;
}
