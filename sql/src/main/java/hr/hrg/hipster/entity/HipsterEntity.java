package hr.hrg.hipster.entity;


import static hr.hrg.hipster.sql.BooleanEnum.*;

import java.lang.annotation.*;

import hr.hrg.hipster.sql.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface HipsterEntity {
	public String table() default "";

    boolean defaultColumnsRequired() default false;	
    
    public BooleanEnum genAnnotations() default DEFAULT;

    public BooleanEnum inspectChange() default DEFAULT;

	public BooleanEnum genMeta() default DEFAULT;

	public BooleanEnum genUpdate() default DEFAULT;

	public BooleanEnum genBuilder() default DEFAULT;

	public BooleanEnum genVisitor() default DEFAULT;

	public BooleanEnum genJson() default DEFAULT;

	public BooleanEnum genMongo() default DEFAULT;

	public BooleanEnum mongoSkipNull() default DEFAULT;

	public BooleanEnum mongoUseFieldName() default DEFAULT;
	
	public BooleanEnum genSql() default DEFAULT;

}
