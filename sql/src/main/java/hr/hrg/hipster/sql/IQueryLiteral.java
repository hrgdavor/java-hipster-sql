package hr.hrg.hipster.sql;

public interface IQueryLiteral extends IQueryPart{

	public String getQueryText();
	public boolean isIdentifier();

}
