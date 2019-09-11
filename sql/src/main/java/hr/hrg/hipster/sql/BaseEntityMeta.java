package hr.hrg.hipster.sql;

import java.util.*;

import hr.hrg.hipster.dao.IEntityMeta;
import hr.hrg.hipster.sql.*;

public abstract class BaseEntityMeta<T,ID,C extends BaseColumnMeta> implements IEntityMeta<T,ID,C>, IQueryLiteral{
	protected final int ordinal;
	protected final String tableName;

	protected final ICustomType<?>[] _typeHandler;
	protected int columnCount;
	private QueryLiteral table;
	protected C[] columnArray;
	protected C[] columnArraySorted;
	protected String[] columnArraySortedStr;

	public BaseEntityMeta(int ordinal, String tableName, QueryLiteral table, C[] columnArray, String[] columnArraySortedStr, C[] columnArraySorted) {
		this.ordinal = ordinal;
		this.tableName = tableName;
		this.columnArray = columnArray;
		this.columnArraySorted = columnArraySorted;
		this.columnArraySortedStr = columnArraySortedStr;
		this.table = table;
		this.columnCount = columnArray.length;
		_typeHandler = new ICustomType<?>[columnCount];
	}

	@Override
	public final int ordinal() {
		return ordinal;
	}

	@Override
	public final ICustomType<?> getTypeHandler(C column) {
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
	
	@Override
	public final C getColumn(String columnName) {
		int pos = Arrays.binarySearch(columnArraySortedStr, columnName);
		return pos < 0 ? null : columnArraySorted[pos];
	}	

	@Override
	public final int getColumnOrdinal(String columnName) {
		return Arrays.binarySearch(columnArraySortedStr, columnName);
	}	

	@Override
	public final C getColumn(int ordinal) {
		return columnArray[ordinal];
	}
	
}
