package hr.hrg.hipster.sql;

public final class QueryLiteral implements IQueryLiteral{

	public static QueryLiteral NULL = new QueryLiteral("NULL");
	public static QueryLiteral EMPTY_QUERY = new QueryLiteral("");

	private final String text;
	private final boolean identifier;

	public QueryLiteral(String text) {
		if(text == null) throw new NullPointerException("Java null value not allowed");
		this.text = text;
		this.identifier = false;
	}

	public QueryLiteral(String text, boolean identifier) {
		this.text = text;
		this.identifier = identifier;
	}

	public static QueryLiteral quoted(String text){
		return new QueryLiteral(text,true);
	}
	
	@Override
	public String getQueryText(){
		return text;
	}

	@Override
	public boolean isEmpty(){
		return text.isEmpty();
	}
	
	@Override
	public String toString() {
		return text;
	}
	
	@Override
	public boolean isIdentifier() {
		return identifier;
	}
}
