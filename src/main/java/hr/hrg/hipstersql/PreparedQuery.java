package hr.hrg.hipstersql;

import java.util.List;

public class PreparedQuery {
	private String query;
	private List<Object> params;
	
	public PreparedQuery(String query, List<Object> params) {
		this.query = query;
		this.params = params;
	}

	public String getQueryString() {
		return query;
	}
	
	public List<Object> getParams() {
		return params;
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
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
	
}