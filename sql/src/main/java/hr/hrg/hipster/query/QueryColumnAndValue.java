package hr.hrg.hipster.query;

import hr.hrg.hipster.entity.*;
import hr.hrg.hipster.sql.*;

public class QueryColumnAndValue implements IQueryPart{

	public static final QueryColumnAndValue EMPTY = new QueryColumnAndValue("", null, "");
	
	private ColumnMeta<?> column;
	private CharSequence expr;
	private CharSequence prefix = "";
	private IQueryValue[] values;

	public QueryColumnAndValue(ColumnMeta<?> column, CharSequence expr, IQueryValue ...values) {
		this.column = column;
		this.expr = expr;
		this.values = values;		
	}
	
	public QueryColumnAndValue(CharSequence prefix, ColumnMeta<?> column, CharSequence expr, IQueryValue ...values) {
		this.column = column;
		this.expr = expr;
		this.values = values;
		if(prefix != null) this.prefix = prefix;
	}
	
	@Override
	public boolean isEmpty() {
		return column == null;
	}

	@Override
	public CharSequence getQueryExpression(HipsterSql hipster) {
		return String.format("%s%s%s%s %s", prefix, hipster.getColumQuote1(), column.getColumnName(), hipster.getColumQuote2(), expr);
	}

	@Override
	public IQueryValue[] getValues() {
		return values;
	}

}
