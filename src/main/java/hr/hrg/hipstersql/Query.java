package hr.hrg.hipstersql;

import java.util.ArrayList;
import java.util.List;

public class Query {

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
	
	public List<Object> getFlatten() {
		List<Object> flat = new ArrayList<>();
		flattenInto(flat, parts);
		return flat;
	}

	public Query getFlattenQuery() {
		List<Object> flat = new ArrayList<>();
		flattenInto(flat, parts);
		return new Query(flat);
	}

	public void flattenInto(List<Object> leftSide, List<Object> rightSide){
		
		int countRight = rightSide.size();
		int countLeft;
		int evenOdd = 0;
		Object part;
		for(int i=0; i<countRight; i++){
			countLeft = leftSide.size();
			part = rightSide.get(i);

			if(part instanceof Query){
				flattenInto(leftSide, ((Query)part).getParts());
				evenOdd = 1;// will be changed to 2 at the end of the loop
			
			}else if( evenOdd % 2 == 0 ){// right: sql code from the rightSide list  

				if(countLeft%2 == 1 && countLeft >0){// left: even: sql code we need to concat
					Object last = leftSide.get(countLeft-1);
					if(last == null) last = "null";
					leftSide.set(countLeft-1, last.toString()+rightSide.get(i));				
				}else{// left: odd: variable, so it is ok just add the sql to the list
					//this also is done when left side is empty
					leftSide.add(rightSide.get(i));
				}
			}else{// right: odd: variable

				if(countLeft%2 == 0){// last element is also variable
					// this should not happen, and to fix, we add empty sql string to be between the variables
					leftSide.add("");
				}
				leftSide.add(rightSide.get(i));
			}
			evenOdd++;
		}
		
	}

	public static final Query implode(List<Query> queries, String glue){		
		return implode(null,queries, glue, null);
	}
	
	public static final Query implode(String prefix, List<Query> queries, String glue){
		return implode(prefix,queries, glue, null);
	}
	
	public static final Query implode(String prefix, List<Query> queries, String glue, String suffix){
		Query query = new Query();

		// remove empty queries
		List<Query> tmp = new ArrayList<Query>();
		for(Query qi:queries) if(!qi.isEmpty()) tmp.add(qi);
		queries = tmp;
		
		if(queries.size() == 0) return query;
		
		if(prefix != null) query.append(prefix);
		
		int count = queries.size();
		for(int i=0; i<count; i++) {
			if(i>0) query.append(glue);
			query.append(queries.get(i));
		}

		if(suffix != null) query.append(suffix);
		return query;
	}

	public static final Query implodeValues(List<? extends Object> queries, String glue){		
		return implodeValues(null,queries, glue, null);
	}
	
	public static final Query implodeValues(String prefix, List<? extends Object> queries, String glue){
		return implodeValues(prefix,queries, glue, null);
	}
	
	public static final Query implodeValues(String prefix, List<? extends Object> values, String glue, String suffix){
		Query query = new Query();
		
		if(values.size() == 0) return query;
		
		if(prefix != null) query.append(prefix);
		else query.append("");

		int count = values.size();
		for(int i=0; i<count; i++) {
			if(i>0) query.append(glue);
			query.appendValue(values.get(i));
		}

		if(suffix != null) query.append(suffix);
		return query;
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
		
		if(countLeft %2 == 1 && !(rightSide[0] instanceof Query) && !(parts.get(countLeft-1) instanceof Query)){
			Object last = parts.get(countLeft-1);
			if(last == null) last = "null";
			parts.set(countLeft-1, last.toString()+rightSide[0]);				
			
			offset++;
		}
		// current parts empty - add all
		// even number of entries - add all because next part is query string like the beginning of new query
		// first one on  rightSide is query - add all because Query can be in any location and after it oddEven is reset
		// last one in current parts is Query - add all because new Query starts after a Query object
		for(int i=offset; i<rightSide.length; i++) {
			parts.add(rightSide[i]);
		}

		return this;
	}

	public void append2(Object ... rightSide){
		int countRight = rightSide.length;
		int countLeft;
		int evenOdd = 0;
		Object part;
		
		for(int i=0; i<countRight; i++){
			countLeft = parts.size();
			part = rightSide[i];

			if(part instanceof Query){
				parts.add(part);
				evenOdd = 1;// will be changed to 2 at the end of the loop
			
			}else if( evenOdd % 2 == 0 ){// right: sql code from the rightSide list  

				if(countLeft%2 == 1 && countLeft >0){// left: even: sql code we need to concat
					Object last = parts.get(countLeft-1);
					if(last == null) last = "null";
					parts.set(countLeft-1, last.toString()+rightSide[i]);				
				}else{// left: odd: variable, so it is ok just add the sql to the list
					//this also is done when left side is empty
					parts.add(rightSide[i]);
				}
			}else{// right: odd: variable

				if(countLeft%2 == 0){// last element is also variable
					// this should not happen, and to fix, we add empty sql string to be between the variables
					parts.add("");
				}
				parts.add(rightSide[i]);
			}
			evenOdd++;
		}
	}
	
	public List<Object> getParts() {
		return parts;
	}

	public boolean isEmpty(){
		if(parts.isEmpty()) return true;
		if(parts.size() == 1){
			Object part = parts.get(0);
			if(part instanceof Query && ((Query)part).isEmpty()) return true;
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
	 * */
	protected StringBuilder build(StringBuilder b, List<Object> parts){

		if(parts.size() == 1 && parts.get(0) instanceof Query) parts = ((Query)parts.get(0)).getParts();
		
		int count = parts.size();
		if(count == 0) return b;

		int evenOdd = 0;
		Object queryPart;
		for(int i=0; i<count; i++){
			queryPart = parts.get(i);
			
			if(queryPart instanceof Query){
				this.build(b, ((Query)queryPart).getParts());
				evenOdd = 1;
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
