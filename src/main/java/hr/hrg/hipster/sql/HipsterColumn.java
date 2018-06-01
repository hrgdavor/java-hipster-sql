package hr.hrg.hipster.sql;

import java.lang.annotation.*;

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
     * (Optional) The name of the table that contains the column. 
     * If absent the column is assumed to be in the primary table.
     * @return table 
     */
    String table() default "";
    
    
    /**
     * (Optional) The class of ICustomType that converts the column 
     * from database to entity and vice versa
     * @return table 
     */
    Class<? extends ICustomType> customType() default ICustomType.class;


    String customTypeKey() default "";
    
}
