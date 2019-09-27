package hr.hrg.hipster.query;

import static hr.hrg.hipster.query.QueryValue.*;

import java.util.*;

import hr.hrg.hipster.dao.*;
import hr.hrg.hipster.sql.*;
import hr.hrg.hipster.type.*;

public class Query{
	private static final String THIS_QUERY_PART_MUST_BE_QUERY_TEXT_AND_NOT = "this query part must be query text, and not: ";

	protected final HipsterSql hipster;

	
	protected StringBuilder queryExpressionBuilder;
	protected IQeuryValue[] values;
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
		this.values = new IQeuryValue[initalCapacity];
	}

	/** Constructor that directly uses the supplied StringBuilder and ArrayList. 
	 * Do not reuse them after supplying them here in the constructor.
	 * 
	 * @param builder builder
	 * @param values parameters for place-holders (be aware that reference is used, the array is not copied)
	 */
	public Query(HipsterSql hipster, StringBuilder builder, int size, IQeuryValue ... values) {
		this(hipster);
		this.queryExpressionBuilder = builder;
		this.values = values;
		this.size = size;
	}

	public IQeuryValue[] getValues() {
		return values;
	}
	
	/** resize internal array
	 * 
	 * @param newSize new size
	 */
	public void resize(int newSize) {
		if(newSize < size) throw new ArrayIndexOutOfBoundsException(newSize);
		IQeuryValue[] tmp = values;
		values = new IQeuryValue[newSize];
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
	
	public final Query add(CharSequence queryExpression, Object value) {
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
	
	/**
	 * 
	 * @param delim
	 * @param values
	 * @return
	 */
	public final Query addValues(CharSequence delim, Object ...values) {
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
	 * @param value
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final Query add(ColumnMeta column, CharSequence queryOperationExpr, Object value) {
		add(column);
		return add(queryOperationExpr, new QueryValue(value, column.getTypeHandler()));
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
	 * @param indexForDebug
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
		
		if(queryPart instanceof IQeuryValue) {
			this.add((IQeuryValue) queryPart);
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
		if(queryPart.isIdentifier())  add(hipster.getColumQuote1());
		add(queryPart.getQueryText());
		if(queryPart.isIdentifier())  add(hipster.getColumQuote2());
		return this;
	}
		
	/** Append a value.
	 * 
	 * @param value value
	 * @return this (builder pattern)
	 */
	public final Query add(IQeuryValue value){
		add("?");
		return withValue(value);
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
	 * @param valuesIn parameters
	 * @return self for builder pattern
	 */
	public final Query add(CharSequence queryExpression, int sizeIn, IQeuryValue ...valuesIn){
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
	public final Query add(CharSequence queryExpression, IQeuryValue ...valuesIn){
		return this.add(queryExpression, valuesIn.length, valuesIn);
	}
	
	/** Prepend prepared statement with corresponding parameters wrapped in IQueryPart each. 
	 * Append but only to the size defined. (values array could be larger with values after size not used)
	 * 
	 * @param queryExpression query expression
	 * @param valuesIn parameters
	 * @return self for builder pattern
	 */
	public final Query addAtBegining(CharSequence queryExpression, int sizeIn, IQeuryValue ...valuesIn){
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
	 * @param query
	 * @return
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
	public IQeuryValue prepValue(Object value) {
		IQeuryValue wrapped = null;
		if(value == null) {
			wrapped = IQeuryValue.NULL;
		}else if(value instanceof IQeuryValue){
			wrapped = (IQeuryValue) value;
		}else {
			ICustomType type = hipster.getTypeSource().getFor(value.getClass());
			if(type == null) throw new HipsterSqlException(this, " value type for not supported "+value.getClass().getName(), null);
			wrapped = new QueryValue(value, type);
		}
		return wrapped;
	}

	public Query withValue(IQeuryValue value) {
		if(value == null) {
			value = IQeuryValue.NULL;
		}

		if(size >= values.length) {
			this.resize(size*2);
		}
		values[size++] = value;
		
		return this;
	}

	public Query withLimit(int offset, int limit) {
		return hipster.appendLimit(offset, limit, queryExpressionBuilder, this);
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
