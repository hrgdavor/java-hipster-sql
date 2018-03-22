package hr.hrg.hipster.sql;

public final class QueryLiteral implements IQueryPart, IQueryLiteral{
	private final String text;
	private final boolean identifier;

	public QueryLiteral(String text) {
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
	public boolean isEmpty() {
		return false;
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
