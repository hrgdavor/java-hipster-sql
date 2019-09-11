package hr.hrg.hipster.sql;

import java.util.ArrayList;
import java.util.List;

public class Query implements IQueryPart{

	/**
	 * Used mainly when appending to a query that ends with a query literal part
	 */
	public static final IQueryPart EMPTY_QUERY_PART = new QueryLiteral("");
	public static final IQueryPart QUERY_SPACE = new QueryLiteral(" ");
	
	protected List<Object> parts;
	/**
	 * If next element that will be added to query is expected to be value.
	 * if false, a query part is expected to be added.
	 */
	protected boolean expectValue = false;
	
	@SuppressWarnings("unchecked")
	public Query(Object ... q){
		if(q.length == 1 && q[0] instanceof List) {
			
			List<Object> tmp = (List<Object>) q[0];
			parts = new ArrayList<>(tmp.size());
			
			for (Object object : q) {
				parts.add(object);
				if(isForceQueryPartNext(object)) {
					expectValue = false;
				}else if(isForceValueNext(object)) {
					expectValue = true;
				}else {
					expectValue = !expectValue;
				}			
			}

		}else {
			parts = new ArrayList<>(q.length);
			append(q);
		}
	}
	
	public Query appendValue(Object value){
		if(!expectValue) {
			parts.add(EMPTY_QUERY_PART);
		}
		parts.add(value);
		expectValue = false;

		return this;
	}
	
	static final boolean isForceQueryPartNext(Object obj){
		if(obj == null) return false;
		return obj instanceof IQueryLiteral
				|| obj instanceof BaseColumnMeta
				|| obj instanceof Query
				|| obj instanceof PreparedQuery;
		
	}

	static final boolean isForceValueNext(Object obj){
		if(obj == null) return false;
		return obj instanceof ICustomType;						
	}
	
	public Query append(Object ... rightSide){
		if(rightSide.length == 0) return this;
		
		int countLeft = parts.size();
		int offset = 0;
		
		
		// fix the situation where next part is expected to be value
		// and since append must work like appending a query, first element will not be a value
		if(expectValue){ 
			// it should not be possible to have expectValue=true and countLeft=0
			Object lastPart = parts.get(countLeft-1);
			boolean forceValue = isForceValueNext(lastPart);
			boolean forceQueryPart = isForceQueryPartNext(lastPart);

			boolean firstAddedIsForceValue = isForceValueNext(rightSide[0]);
			boolean firstAddedIsForceQueryPart = isForceQueryPartNext(rightSide[0]);
			
			if(forceValue && (firstAddedIsForceValue ||  firstAddedIsForceQueryPart)) 
				throw new RuntimeException("Last element in current query expects a value, but first element in appended query is not a value");
			
			if(!forceQueryPart && !forceValue && !firstAddedIsForceValue){
				// add dummy to avoid  rightSide[0] being interpreted as variable
				parts.add(Query.EMPTY_QUERY_PART);				
			}
			
			expectValue = false;
		}

		// current parts empty - add all (same as even number of entries)
		// even number of entries - add all because next part is query string like the beginning of new query
		for(int i=offset; i<rightSide.length; i++) {
			parts.add(rightSide[i]);
			if(isForceQueryPartNext(rightSide[i])) {
				expectValue = false;
			}else if(isForceValueNext(rightSide[i])) {
				expectValue = true;
			}else {
				expectValue = !expectValue;
			}
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
			if(part instanceof IQueryPart && ((IQueryPart)part).isEmpty()) return true;
			if(part != null && part.toString().isEmpty()) return true;
		}
		return false;
	}

	/** Generate immutable version of this Query object. 
	 * Does not guarantee immutability of query elements 
	 * (aside from other Query objects that will be converted to immutable versions)  
	 * 
	 * @return immutable version of this query
	 */
	public ImmutableQuery immutable() {
		Object[] newParts = new Object[parts.size()];
		
		Object part = null;
		for(int i=0; i<newParts.length; i++) {
			part = parts.get(i);
			if(part != null && part instanceof Query) {
				newParts[i] = ((Query)part).immutable();
			}else {
				newParts[i] = part;
			}
		}
		
		return new ImmutableQuery(ImmutableList.safe(newParts));
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
			
			if(queryPart instanceof IQueryLiteral){
				IQueryLiteral queryLiteral = (IQueryLiteral) queryPart;
				if(queryLiteral.isIdentifier()) b.append('"');
				b.append(queryLiteral.getQueryText());
				if(queryLiteral.isIdentifier()) b.append('"');
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

	public static final class ImmutableQuery extends Query {

		public ImmutableQuery(ImmutableList parts) {
			this.parts = parts;
		}
		
		@Override
		public ImmutableQuery immutable() {
			return this;
		}
	}
	
	public static ImmutableQuery immutable(Object ...parts) {
		return new ImmutableQuery(ImmutableList.safe(parts));
	}
	
	
}
