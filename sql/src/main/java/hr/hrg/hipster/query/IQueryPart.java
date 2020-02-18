package hr.hrg.hipster.query;

import java.util.*;

import hr.hrg.hipster.sql.*;

public interface IQueryPart {

	public boolean isEmpty();
	
	public CharSequence getQueryExpression(HipsterSql hipster);

	public IQueryValue[] getValues();
}
