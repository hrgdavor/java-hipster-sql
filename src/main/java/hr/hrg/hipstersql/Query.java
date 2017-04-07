package hr.hrg.hipstersql;

import java.util.ArrayList;
import java.util.List;

public class Query implements QueryPart{

	/**
	 * Used mainly when appending to a query that ends with a query literal part
	 */
	public static final QueryPart EMPTY_QUERY_PART = new QueryLiteral("");
	
	List<Object> parts;
	
	@SuppressWarnings("unchecked")
	public Query(Object ... q){
		if(q.length == 1 && q[0] instanceof List) {
			parts = (List<Object>) q[0];

		}else {
			parts = new ArrayList<>(q.length);
			for (Object object : q) {
				parts.add(object);
			}
		}
	}
	
	public Query appendValue(Object value){
		int countLeft = parts.size();
		if(countLeft %2 == 0) {
			parts.add("");
		}
		parts.add(value);

		return this;
	}
	
	public Query append(Object ... rightSide){
		int countLeft = parts.size();
		int offset = 0;

		// first one on  rightSide is QueryPart - add all because Query can be in any location and after it oddEven is reset
		// last one in current parts is QueryPart - add all because new Query starts after a Query object
		if(countLeft %2 == 1){
			if(!(parts.get(countLeft-1) instanceof QueryPart || rightSide[0] instanceof QueryPart)){
				// add dummy to avoid  rightSide[0] being interpreted as variable
				parts.add(Query.EMPTY_QUERY_PART);
			}
		}
		// current parts empty - add all (same as even number of entries)
		// even number of entries - add all because next part is query string like the beginning of new query
		for(int i=offset; i<rightSide.length; i++) {
			parts.add(rightSide[i]);
		}

		return this;
	}

	public List<Object> getParts() {
		return parts;
	}

	public boolean isEmpty(){
		if(parts.isEmpty()) return true;
		if(parts.size() == 1){
			Object part = parts.get(0);
			if(part instanceof QueryPart && ((QueryPart)part).isEmpty()) return true;
			if(part != null && part.toString().isEmpty()) return true;
		}
		return false;
	}

	public String toString(){
		return build(new StringBuilder(), parts).toString();
	}

	/** Build an SQL string suitable for debugging and printing to log files or testing the resulting query.<br>
	 * <b>MUST NOT BE USED FOR GENERATING QUERIES THAT GO TO DATABASE.</b><br>
	 * the escape function only changes single quotes to double quotes in strings and is not protection against SQL injection.
	 * 
	 * @param b StringBuilder to append the query to
	 * @param parts query parts
	 * @return the same StringBuilder provided as first parameter
	 */
	protected StringBuilder build(StringBuilder b, List<Object> parts){

		if(parts.size() == 1 && parts.get(0) instanceof Query) parts = ((Query)parts.get(0)).getParts();
		
		int count = parts.size();
		if(count == 0) return b;

		int evenOdd = 0;
		Object queryPart;
		for(int i=0; i<count; i++){
			queryPart = parts.get(i);
			
			if(queryPart instanceof QueryLiteral){
				b.append(((QueryLiteral)queryPart).getText());
				evenOdd = 1;// will be changed to 2 at the end of the loop			
			}else if(queryPart instanceof Query){
				this.build(b, ((Query)queryPart).getParts());
				evenOdd = 1;
			}else if(queryPart instanceof PreparedQuery){
				PreparedQuery prepared = (PreparedQuery) queryPart;
				b.append(prepared.getQueryString());
			
			}else if(evenOdd %2 == 0){
				b.append(queryPart);// all even index parts must be strings
			}else{
				HipsterSql.qValue(b, queryPart);
			}
			evenOdd++;
		}
		return b;
	}

	
}
