package hr.hrg.hipster.query;

import java.util.*;

import hr.hrg.hipster.type.*;

public interface IQueryPart {

	public boolean isEmpty();
	
	@SuppressWarnings("rawtypes")
	public List getQueryParameters();

	public List<ICustomType<?>> getQuerySetters();
}
