package hr.hrg.hipster.query;

import hr.hrg.hipster.sql.*;

public class QueryRepeat extends Query{

	private String queryString;

	/** Constructor that directly uses the supplied values array. 
	 * Do not reuse it after supplying it here in the constructor.
	 * 
	 * @param hipster HipsterSql
	 * @param queryString query expression
	 * @param values parameters for place-holders (be aware that reference is used, the array is not copied)
	 */
	public QueryRepeat(HipsterSql hipster, String queryString, IQueryValue ... values) {
		super(hipster, null, values.length, values);
		this.queryString = queryString;
	}

	/** Used by clone. size is reset, so it must be filled with values before using
	 * 
	 * @param query query
	 */
	QueryRepeat(QueryRepeat query) {
		super(query.hipster, null, 0, query.values.clone());
		this.queryString = query.queryString;
	}

	/** Size is reset, so it must be filled with values before using
	 * 
	 * @param query query 
	 */
	public QueryRepeat(Query query) {
		super(query.hipster, null, 0, toSize(query.values, query.size));
		queryString = query.queryExpressionBuilder.toString();
	}

	public static IQueryValue[] toSize(IQueryValue[] values, int size) {
		IQueryValue[] ret = new IQueryValue[size];
		System.arraycopy(values, 0, ret, 0, size);
		return ret;
	}
	
	@Override
	public Query add(char queryText) { return this;}
	
	@Override
	public Query add(CharSequence queryText) { return this;}
	
	@Override
	public Query addAtBegining(CharSequence queryText) { return this;}
	
	@Override
	public void resize(int newSize) {
		throw new HipsterSqlException(this, "repeat-query can not resized/changed number of values. This marks unintended usage of repeat-query. You are either addding more values than neeeded, or not resetting the repeat-query", null);
	}

	public QueryRepeat withValues(Object ...valuesIn) {
		if(valuesIn.length != values.length) throw new ArrayIndexOutOfBoundsException("Exact numer of values must be provided");
		for(int i=valuesIn.length-1; i>=0; i--) {
			try {
				values[i] = prepValue(valuesIn[i]);				
			} catch (Exception e) {
				throw new HipsterSqlException(this, "Problem preparing value "+values[i], e);
			}
		}
		return this;
	}
	
//	@Override
//	public QueryRepeat init() {
//		this.size = 0;
//		return this;
//	}

	@Override
	public QueryRepeat clone() {
		return new QueryRepeat(this);
	}

	@Override
	public QueryRepeat toRepeatable(){
		return this;
	}

	@Override
	public CharSequence getQueryExpression() {
		return queryString;
	}
	
	@Override
	public boolean isEmpty() {
		return queryString.isEmpty();
	}
}
