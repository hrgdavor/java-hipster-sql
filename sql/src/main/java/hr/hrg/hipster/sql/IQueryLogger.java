package hr.hrg.hipster.sql;

import java.util.*;

public interface IQueryLogger {
	boolean isLogging();
	boolean logQuery(String query, Throwable trace, long time, long duration, List<?> params);
}
