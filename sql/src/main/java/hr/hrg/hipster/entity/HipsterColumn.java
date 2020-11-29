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

    /**
     * (Optional) sql expression for the column. If defined the column name 
     * is added as alias after the expression.
     * @return sql 
     */
    String sql() default "";

    /**
     * (Optional) The name of the _table that contains the column. 
     * If absent the column is assumed to be in the primary _table.
     * @return _table 
     */
    String table() default "";
    
    
    /**
     * (Optional) The class of ICustomType that converts the column 
     * from database to entity and vice versa
     * @return _table 
     */
    @SuppressWarnings("rawtypes")
	Class<? extends ICustomType> customType() default ICustomType.class;


    String customTypeKey() default "";

    BooleanEnum required() default BooleanEnum.DEFAULT;
    
    
    /**
     * Should this method be skipped
     * @return if method should be skipped 
     */
    boolean skip() default false;

}
