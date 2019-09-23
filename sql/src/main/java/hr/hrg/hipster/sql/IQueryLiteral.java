package hr.hrg.hipster.sql;

public interface IQueryLiteral {

	public CharSequence getQueryText();
	public boolean isIdentifier();

}
