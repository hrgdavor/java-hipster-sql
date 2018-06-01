package hr.hrg.hipster.sql;

import hr.hrg.hipster.dao.IEntityMeta;
import hr.hrg.hipster.sql.*;

public abstract class BaseEntityMeta<T,ID,E extends BaseColumnMeta> implements IEntityMeta<T,ID,E>, IQueryLiteral{
	protected final int ordinal;
	protected final String tableName;

	protected final ICustomType<?>[] _typeHandler;
	protected int columnCount;
	private QueryLiteral table;

	public BaseEntityMeta(int ordinal, String tableName, int columnCount) {
		this.ordinal = ordinal;
		this.tableName = tableName;
		this.table = new QueryLiteral(tableName, true);
		this.columnCount = columnCount;
		_typeHandler = new ICustomType<?>[columnCount];
	}

	public BaseEntityMeta(int ordinal, String tableName, QueryLiteral table, int columnCount) {
		this.ordinal = ordinal;
		this.tableName = tableName;
		this.table = table;
		this.columnCount = columnCount;
		_typeHandler = new ICustomType<?>[columnCount];
	}

	@Override
	public final int ordinal() {
		return ordinal;
	}

	@Override
	public final ICustomType<?> getTypeHandler(E column) {
		return _typeHandler[column.ordinal()];
	}

	@Override
	public final ICustomType<?> getTypeHandler(int ordinal) {
		return _typeHandler[ordinal];
	} 

	@Override
	public final String getQueryText() {
		return tableName;
	}
	
	@Override
	public final boolean isIdentifier() {
		return true;
	}
	
	@Override
	public final boolean isEmpty() {
		return false;
	}
	
	@Override
	public final String getTableName() {
		return tableName;
	}
	
	@Override
	public QueryLiteral getTable() {
		return table;
	}
	
	@Override
	public int getColumnCount() {
		return columnCount;
	}

}
