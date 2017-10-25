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
     */
    String name() default "";

    /**
     * (Optional) sql expression for the column. If defined the column name 
     * is added as alias after the expression.
     */
    String sql() default "";

    /**
     * (Optional) The name of the table that contains the column. 
     * If absent the column is assumed to be in the primary table.
     */
    String table() default "";
    
}
