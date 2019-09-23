package hr.hrg.hipster.sql;

import java.util.*;

public interface IQueryPart {

	public boolean isEmpty();
	
	public List getQueryParameters();

	public List<ICustomType<?>> getQuerySetters();
}
