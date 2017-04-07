package hr.hrg.hipstersql;

public final class QueryLiteral implements QueryPart{
	private final String text;

	public QueryLiteral(String text) {
		this.text = text;
	}

	public String getText() {
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
