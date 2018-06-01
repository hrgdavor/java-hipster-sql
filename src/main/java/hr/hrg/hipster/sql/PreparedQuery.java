package hr.hrg.hipster.sql;

import java.util.*;

public class PreparedQuery implements IQueryPart{
	protected StringBuilder stringBuilder;
	protected List<Object> params = new ArrayList<>();
	protected List<ICustomType<?>> setters = new ArrayList<>();
	protected final TypeSource typeSource;
	
	public PreparedQuery(TypeSource typeSource) {
		this.typeSource = typeSource;
		
	}

	public PreparedQuery(TypeSource typeSource, String query, List<Object> params) {
		this(typeSource);
		this.stringBuilder = new StringBuilder();
		this.appendList(query, params);
	}

	public PreparedQuery(TypeSource typeSource, String query, Object ...params) {
		this(typeSource);
		this.stringBuilder = new StringBuilder();
		this.append(query, params);
	}
	
	/**
	 * Internal constructor that actually uses the supplied StringBuilder and ArrayList. 
	 * Do not reuse them after supplying them here in the constructor.
	 * @param builder builder
	 * @param params parameters for place-holders
	 */
	public PreparedQuery(TypeSource typeSource, StringBuilder builder, ArrayList<Object> params, List<ICustomType<?>> setters) {
		this(typeSource);
		this.stringBuilder = builder;
		this.params = params;
		this.setters = setters;
	}

	public String getQueryString() {
		return stringBuilder.toString();
	}
	
	public List<Object> getParams() {
		return params;
	}
	
	public PreparedQuery append(String query, Object ...params){
		this.stringBuilder.append(query);
		QueryUtil.addToList(this.params, params);
		return this;
	}
	
	public PreparedQuery appendList(String query, List<Object> params){
		this.stringBuilder.append(query);
		//QueryUtil.addToList(this.params, params);
		this.params.addAll(params);
		return this;
	}

	StringBuilder getQueryStringBuilder() {
		return stringBuilder;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		String query = this.stringBuilder.toString();
		int idx = query.indexOf('?');
		int offset = 0;
		int index = 0;
		while(idx != -1) {
			b.append(query.substring(offset,idx));
			HipsterSql.qValue(b, params.get(index));
			
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
		return stringBuilder.length() == 0;
	}
	
}
