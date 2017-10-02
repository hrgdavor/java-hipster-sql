package hr.hrg.hipster.sql;

public final class QueryLiteral implements IQueryPart, IQueryLiteral{
	private final String text;

	public QueryLiteral(String text) {
		this.text = text;
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
}
