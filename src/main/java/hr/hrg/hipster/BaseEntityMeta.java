package hr.hrg.hipster;

import hr.hrg.hipster.dao.IEntityMeta;
import hr.hrg.hipster.sql.IColumnMeta;
import hr.hrg.hipster.sql.IPreparedSetter;
import hr.hrg.hipster.sql.IQueryLiteral;
import hr.hrg.hipster.sql.IResultGetter;

public abstract class BaseEntityMeta<T,ID,E extends IColumnMeta> implements IEntityMeta<T,ID,E>, IQueryLiteral{
	protected final int ordinal;
	protected final String tableName;

	protected final IResultGetter<?>[] _resultGetter;
	protected final IPreparedSetter<?>[] _preparedSetter;

	public BaseEntityMeta(int ordinal, String tableName, int columnCount) {
		this.ordinal = ordinal;
		this.tableName = tableName;
		_resultGetter = new IResultGetter[columnCount];
		_preparedSetter = new IPreparedSetter[columnCount];
	}

	@Override
	public final int ordinal() {
		return ordinal;
	}

	public IPreparedSetter<?> getPreparedSetter(int ordinal) {
		return _preparedSetter[ordinal];
	}

	public IPreparedSetter<?> getPreparedSetter(IColumnMeta column) {
		return _preparedSetter[column.ordinal()];
	}

	public IResultGetter<?> getResultGetter(int ordinal) {
		return _resultGetter[ordinal];
	}

	public IResultGetter<?> getResultGetter(IColumnMeta column) {
		return _resultGetter[column.ordinal()];
	}	  
	
	@Override
	public String getQueryText() {
		return tableName;
	}
	
	@Override
	public boolean isIdentifier() {
		return true;
	}
	
	@Override
	public String getTableName() {
		return tableName;
	}
}
