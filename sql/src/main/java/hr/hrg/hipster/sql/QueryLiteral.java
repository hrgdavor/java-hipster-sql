package hr.hrg.hipster.sql;

/** Class that encapsulates simple expressions like NULL or identifiers like column/table names. 
 * 
 * @author hrg
 *
 */
public final class QueryLiteral implements IQueryLiteral{

	public static QueryLiteral NULL = new QueryLiteral("NULL");
	public static QueryLiteral EMPTY_QUERY = new QueryLiteral("");

	private final CharSequence text;
	private final boolean identifier;

	/** Create simple query literal that is added to queries unchanged
	 * 
	 * @param text query
	 */
	public QueryLiteral(CharSequence text) {
		if(text == null) throw new NullPointerException("Java null value not allowed, use hr.hrg.hipster.sql.NULL if u want NULL sql expression");
		this.text = text;
		this.identifier = false;
	}
	
	
	/** QueryOld literal for identifiers.
	 *  Identifier wrapped into a QueryLiteral that QueryOld building code will  understand (will add proper quotes)
	 * @param text name
	 */
	public QueryLiteral(String text, boolean identifier) {
		this.text = text;
		this.identifier = identifier;
	}

	/** Shortcut to create a query literal for identifiers.
	 * Identifier wrapped into a QueryLiteral tha QueryOld building code will  understand (will add proper quotes)
	 * 
	 * @param identifier name
	 */
	public static QueryLiteral identifier(String identifier){
		return new QueryLiteral(identifier,true);
	}
	
	/** query 
	 * 
	 */
	@Override
	public CharSequence getQueryText(){
		return text;
	}

	@Override
	public boolean isIdentifier() {
		return identifier;
	}

	@Override
	public String toString() {
		return identifier ? "\""+text.toString()+"\"" : text.toString();
	}


}
