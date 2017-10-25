package hr.hrg.hipster.sql;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface HipsterVisitor {
	String value() default "";
}
