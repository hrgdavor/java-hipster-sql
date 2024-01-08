package hr.hrg.hipster.query;

import static hr.hrg.hipster.query.QueryValue.*;

import java.util.*;

import hr.hrg.hipster.entity.*;
import hr.hrg.hipster.sql.*;
import hr.hrg.hipster.type.*;

public class Query{
	private static final String THIS_QUERY_PART_MUST_BE_QUERY_TEXT_AND_NOT = "this query part must be query text, and not: ";

	protected final HipsterSql hipster;

	
	protected StringBuilder queryExpressionBuilder;
	protected IQueryValue[] values;
	protected int size = 0;

	
	public Query(HipsterSql hipster) {
		this(hipster, new StringBuilder(), 16);
	}
	
	public Query(HipsterSql hip, int initalCapacity) {
		this(hip, new StringBuilder(), initalCapacity);
	}
	
	public Query(HipsterSql hipster, StringBuilder builder, int initalCapacity) {
		this.hipster = hipster;
		this.queryExpressionBuilder = builder;
		this.values = new IQueryValue[initalCapacity];
	}

	/** Constructor that directly uses the supplied StringBuilder and ArrayList. 
	 * Do not reuse them after supplying them here in the constructor. Size parameter
	 * is used when values array is larger than needed, and only subset is used in the query
	 * 
	 * @param hipster hister
	 * @param builder builder
	 * @param size size
	 * @param values parameters for place-holders (be aware that reference is used, the array is not copied)
	 */
	public Query(HipsterSql hipster, StringBuilder builder, int size, IQueryValue ... values) {
		this(hipster);
		this.queryExpressionBuilder = builder;
		this.values = values;
		this.size = size;
	}

	/** Constructor that directly uses the supplied StringBuilder and ArrayList. 
	 * Do not reuse them after supplying them here in the constructor.
	 * 
	 * @param hipster hister
	 * @param builder builder
	 * @param values parameters for place-holders (be aware that reference is used, the array is not copied)
	 */
	public Query(HipsterSql hipster, StringBuilder builder,  IQueryValue ... values) {
		this(hipster);
		this.queryExpressionBuilder = builder;
		this.values = values;
		this.size = values.length;
	}

	public IQueryValue[] getValues() {
		return values;
	}
	
	/** resize internal array
	 * 
	 * @param newSize new size
	 */
	public void resize(int newSize) {
		if(newSize < size) throw new ArrayIndexOutOfBoundsException(newSize);
		IQueryValue[] tmp = values;
		values = new IQueryValue[newSize];
		System.arraycopy(tmp, 0, values, 0, size);
	}

	@SuppressWarnings("rawtypes")
	public Query addColumns(EntityMeta meta){
		addPartsList(",", meta.getColumns());
		return this;
	}

	/** 
	 * 
	 * @param queryParts parts to combine into a query
	 * @return self for builder pattern
	 */
	public Query addParts(Object ... queryParts){
		
		boolean queryPartNext = true;
		
		int length = queryParts.length;
		
		for(int i=0; i<length; i++) {
			if(queryPartNext) {
				queryPartNext = addAsQueryExpressionPart(queryParts[i],i);
			}else {
				 addAsQueryValuePart(queryParts[i],i);
				 queryPartNext = true;
			}
		}
		
		return this;
	}

	/** Add parts with a delimiter between them.
	 * 
	 * @param delim delimiter
	 * @param queryParts parts
	 * @return self for builder pattern
	 */
	public Query addPartsArray(CharSequence delim, Object ... queryParts) {
		int length = queryParts.length;
		
		for(int i=0; i<length; i++) {
			Object queryPart = queryParts[i];
			if(i>0) add(delim);
			if(!addAndCheckIfQueryPart(queryPart)) {			
				add('?');
				return withValue(queryPart);
			}
		}
		return this;
	}
	
	/** Add parts with a delimiter between them.
	 * 
	 * @param delim delimiter
	 * @param queryParts parts
	 * @return self for builder pattern
	 */
	public Query addPartsList(CharSequence delim, List<?> queryParts) {
		int length = queryParts.size();
		
		for(int i=0; i<length; i++) {
			Object queryPart = queryParts.get(i);
			if(i>0) add(delim);
			if(!addAndCheckIfQueryPart(queryPart)) {			
				add('?');
				return withValue(queryPart);
			}
		}
		return this;
	}
	
	public final <T> Query add(CharSequence queryExpression, T value) {
		add(queryExpression);
		if(!addAndCheckIfQueryPart(value)) {			
			add('?');
			return withValue(value);
		}
		return this;
	}
	
	public final Query add(CharSequence queryExpression, int value) {
		add(queryExpression); add('?');
		return withValue(value);
	}
	
	
	public final Query add(CharSequence queryExpression, long value) {
		add(queryExpression); add('?');
		return withValue(value);
	}
	
	public final Query add(CharSequence queryExpression, float value) {
		add(queryExpression); add('?');
		return withValue(value);
	}
	
	public final Query add(CharSequence queryExpression, double value) {
		add(queryExpression); add('?');
		return withValue(value);
	}

	public final Query add(CharSequence queryExpression, boolean value) {
		add(queryExpression); add('?');
		return withValue(value);
	}
	
	
	public final Query addValue(Object value) {
		add('?');
		return withValue(value);
	}
	
	@SafeVarargs
	public final <T> Query addValues(CharSequence delim, T ...values) {
		for(int i=0; i<values.length; i++) {
			if(i > 0) add(delim);
			add('?');
			withValue(values[i]);
		}
		return this;
	}
	
	public final Query addValuesList(CharSequence delim, List<?> values) {
		int length = values.size();
		
		for(int i=0; i<length; i++) {
			if(i > 0) add(delim);
			add('?');
			withValue(values.get(i));
		}
		return this;
	}

	/** Add column, operation, columnValue.
	 * Column definition is used to extract {@link ICustomType} for the value, so
	 * it matches what would happen in ORM scenario.
	 * 
	 * @param column column
	 * @param queryOperationExpr queryOperationExpr
	 * @param value value value
	 * @return query
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final Query add(ColumnMeta column, CharSequence queryOperationExpr, Object value) {
		add(column);
		return add(queryOperationExpr, new QueryValue(value, column.getTypeHandler()));
	}
	
	
	/** Add column, operation
	 * @param column column
	 * @param queryOperationExpr queryOperationExpr
	 * @return query
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final Query add(ColumnMeta column, CharSequence queryOperationExpr) {
		add(column);
		return add(queryOperationExpr);
	}


	public final void addAsQueryValuePart(Object queryPart, int indexForDebug) {

		if(!addAndCheckIfQueryPart(queryPart)) {
			this.addValue(queryPart);
		}		
	}

	/** append to query expecting the part to be an expression.
	 *  String here is treated as expression
	 * 
	 * @param part to append
	 * @param indexForDebug index for debug
	 * @return true if this is an expression (if called has multiple parts, then next part is a value)
	 */
	public final boolean addAsQueryExpressionPart(Object part, int indexForDebug) {
		if(part == null) throw new HipsterSqlException(this,"#0"+THIS_QUERY_PART_MUST_BE_QUERY_TEXT_AND_NOT+null,null);

		if(addAndCheckIfQueryPart(part)){
			return true;//not a textual expression, so next part is textual expression
		}

		if(part instanceof CharSequence) {
			this.add((CharSequence)part);
			return false;// textual expression, so next part is value
		}

		throw new HipsterSqlException(this,"#"+indexForDebug+THIS_QUERY_PART_MUST_BE_QUERY_TEXT_AND_NOT+part,null);
	}
	

	/** append to query expecting the part to be an expression.
	 *  String here is treated as value
	 * 
	 * @param queryPart to append
	 * @return if this is an expression (if called has multiple parts, then next part is a value)
	 */
	private boolean addAndCheckIfQueryPart(Object queryPart) {
		if(queryPart instanceof Query) {
			this.add((Query) queryPart);
			return true;// query part
		}
		
		if(queryPart instanceof IQueryLiteral) {
			this.add((IQueryLiteral) queryPart);
			return true;// query part
		}
		
		if(queryPart instanceof IQueryPart) {
			this.add((IQueryPart) queryPart);
			return true;// query part
		}
		
		if(queryPart instanceof IQueryValue) {
			this.add((IQueryValue) queryPart);
			return true;// query part
		}
		
		return false;
	}
	
	/** Append query expression text.
	 * 
	 * @param queryPart expression
	 * @return this (builder pattern)
	 */
	public final Query add(IQueryLiteral queryPart){
		if(queryPart.isIdentifier())  
			add(hipster.getColumQuote1());
		
		add(queryPart.getQueryText());
		
		if(queryPart.isIdentifier())  
			add(hipster.getColumQuote2());
		return this;
	}
		
	/** Append a value.
	 * 
	 * @param value value
	 * @return this (builder pattern)
	 */
	public final Query add(IQueryValue value){
		add("?");
		return withValue(value);
	}
	
	/** Append a value.
	 * 
	 * @param part value
	 * @return this (builder pattern)
	 */
	public final Query add(IQueryPart part){
		if(part.isEmpty()) return this;
		
		IQueryValue[] v = part.getValues();
		return add(part.getQueryExpression(hipster), v.length, v);
	}

	/** Append another query.
	 * 
	 * @param query query
	 * @return this (builder pattern)
	 */
	public final Query add(Query query){
		return add(query.getQueryExpression(), query.size, query.values);
	}
	
	/** Append prepared statement with corresponding parameters wrapped in IQueryPart each. 
	 * Append but only to the size defined. (values array could be larger with values after size not used)
	 * 
	 * @param queryExpression query expression
	 * @param sizeIn max size
	 * @param valuesIn parameters
	 * @return self for builder pattern
	 */
	public final Query add(CharSequence queryExpression, int sizeIn, IQueryValue ...valuesIn){
		this.add(queryExpression);
		if(sizeIn > 0) {				
			if(this.size + sizeIn > this.values.length) {
				this.resize(this.size + sizeIn);
			}
			System.arraycopy(valuesIn, 0, values, this.size, sizeIn);
			size += sizeIn;
		}
		return this;
	}
	
	public Query add(char queryExpression) {
		queryExpressionBuilder.append(queryExpression);
		return this;
	}
	
	public Query add(CharSequence queryExpression) {
		queryExpressionBuilder.append(queryExpression);
		return this;
	}
	
	/** append prepared statement with all parameters wrapped in IQueryPart.
	 * 
	 * @param queryExpression query string
	 * @param valuesIn parameters
	 * @return self for builder pattern
	 */
	public final Query add(CharSequence queryExpression, IQueryValue ...valuesIn){
		return this.add(queryExpression, valuesIn.length, valuesIn);
	}
	
	/** Prepend prepared statement with corresponding parameters wrapped in IQueryPart each. 
	 * Append but only to the size defined. (values array could be larger with values after size not used)
	 * 
	 * @param queryExpression query expression
	 * @param sizeIn max size
	 * @param valuesIn parameters
	 * @return self for builder pattern
	 */
	public final Query addAtBegining(CharSequence queryExpression, int sizeIn, IQueryValue ...valuesIn){
		this.addAtBegining(queryExpression);
		if(sizeIn > 0){
			if(this.size + sizeIn > this.values.length) {
				this.resize(this.size + sizeIn);
			}
			System.arraycopy(valuesIn, 0, values, this.size, sizeIn);
			size += sizeIn;
		}
		return this;
	}
	
	/** Prepend a query
	 * 
	 * @param query query
	 * @return query
	 */
	public final Query addAtBegining(Query query){
		return addAtBegining(query.getQueryExpression(), query.size, query.values);
	}
	
	
	public Query addAtBegining(CharSequence queryExpression) {
		queryExpressionBuilder.insert(0,queryExpression);
		return this;
	}
	
	/** Just add a primitive value without changing the expression, and also avoid boxing. 
	 * Adding value normally adds "?" placeholder for value, but this method is used when
	 * we add one or more placeholders and want to add values for them. 
	 * 
	 * @param value value
	 * @return this  (builder pattern)
	 */
	public final Query withValue(int value) {
		return withValue(v(value));
	}
	
	/** Just add a primitive value without changing the expression, and also avoid boxing. 
	 * Adding value normally adds "?" placeholder for value, but this method is used when
	 * we add one or more placeholders and want to add values for them. 
	 * 
	 * @param value value
	 * @return this (builder pattern)
	 */
	public final Query withValue(long value) {
		return withValue(v(value));
	}

	/** Just add a primitive value without changing the expression, and also avoid boxing. 
	 * Adding value normally adds "?" placeholder for value, but this method is used when
	 * we add one or more placeholders and want to add values for them. 
	 * 
	 * @param value value
	 * @return this (builder pattern)
	 */
	public final Query withValue(double value) {
		return withValue(v(value));
	}

	/** Just add a primitive value without changing the expression, and also avoid boxing. 
	 * Adding value normally adds "?" placeholder for value, but this method is used when
	 * we add one or more placeholders and want to add values for them. 
	 * 
	 * @param value value
	 * @return this (builder pattern)
	 */
	public final Query withValue(float value) {
		return withValue(v(value));
	}

	/** Just add a primitive value without changing the expression, and also avoid boxing. 
	 * Adding value normally adds "?" placeholder for value, but this method is used when
	 * we add one or more placeholders and want to add values for them. 
	 * 
	 * @param value value
	 * @return this (builder pattern)
	 */
	public final Query withValue(boolean value) {
		return withValue(v(value));
	}

	/** Just add a value without changing the expression. 
	 * Adding value normally adds "?" placeholder for value, but this method is used when
	 * we add one or more placeholders and want to add values for them. 
	 * 
	 * @param value value
	 * @return this (builder pattern)
	 */
	public Query withValue(Object value) {
		return withValue(prepValue(value));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public IQueryValue prepValue(Object value) {
		IQueryValue wrapped = null;
		if(value == null) {
			wrapped = IQueryValue.NULL;
		}else if(value instanceof IQueryValue){
			wrapped = (IQueryValue) value;
		}else {
			if(hipster == null) throw new RuntimeException("Query without hipster can not add values as TypeSource is not available then");
			ICustomType type = hipster.getTypeSource().getForRequired(value.getClass());
			wrapped = new QueryValue(value, type);
		}
		return wrapped;
	}

	public Query withValue(IQueryValue value) {
		if(value == null) {
			value = IQueryValue.NULL;
		}

		if(size >= values.length) {
			this.resize(size*2);
		}
		values[size++] = value;
		
		return this;
	}

	public Query withLimit(int offset, int limit) {
		hipster.appendLimit(offset, limit, queryExpressionBuilder);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		String query = this.getQueryExpression().toString();
		int idx = query.indexOf('?');
		int offset = 0;
		int index = 0;
		while(idx != -1) {
			b.append(query.substring(offset,idx));
			b.append(values[index]);
			
			offset = idx+1;
			index++;
			idx = query.indexOf('?', offset);
		}

		if(offset < query.length()) {
			b.append(query.substring(offset));
		}
		
		return b.toString();
	}

	public boolean isEmpty() {
		return queryExpressionBuilder.length() == 0;
	}

	/** Clone this query.
	 * Internal query string is cloned and can be appended.
	 * Internal values array is cloned (shallow values are copied) and can be changed further.
	 * */
	public Query clone(){
		return new Query(hipster, new StringBuilder(queryExpressionBuilder), size, values.clone());
	}

//	/** No-op method, used by repeat subclass. 
//	*/
//	public Query init() {
//		return this;
//	}
	
	public QueryRepeat toRepeatable() {
		return new QueryRepeat(this);
	}

	/** 
	 * @return accumulated query string
	 */
	public CharSequence getQueryExpression() {
		return queryExpressionBuilder;
	}

	public int getSize() {
		return size;
	}
}
