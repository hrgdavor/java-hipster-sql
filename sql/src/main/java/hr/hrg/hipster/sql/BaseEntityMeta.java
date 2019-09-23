package hr.hrg.hipster.sql;

import java.sql.*;
import java.util.*;

import hr.hrg.hipster.dao.*;

@SuppressWarnings("rawtypes")
public abstract class BaseEntityMeta<T,ID, C extends BaseColumnMeta> implements IEntityMeta<T,ID>, IQueryLiteral{

	protected final int _ordinal;
	protected final String _tableName;
	protected final Class<T> _entityClass;

	protected ICustomType<?>[] _typeHandler;
	protected int _columnCount;
	protected C[] _columnArray;
	protected C[] _columnArraySorted;
	protected String[] _columnArraySortedStr;

	public BaseEntityMeta(int ordinal, String tableName, Class<T> entityClass) {
		this._ordinal = ordinal;
		this._tableName = tableName;
		this._entityClass = entityClass;
	}

	public BaseEntityMeta(int ordinal, String tableName, Class<T> entityClass, C[] columnArray, String[] columnArraySortedStr, C[] columnArraySorted) {
		this(ordinal, tableName, entityClass);

		this._columnArray = columnArray;
		this._columnArraySorted = columnArraySorted;
		this._columnArraySortedStr = columnArraySortedStr;
		this._columnCount = columnArray.length;
		_typeHandler = new ICustomType<?>[_columnCount];
	}

	@Override
	public final int ordinal() {
		return _ordinal;
	}

	@Override
	public final ICustomType<?> getTypeHandler(BaseColumnMeta column) {
		return _typeHandler[column.ordinal()];
	}

	@Override
	public final ICustomType<?> getTypeHandler(int ordinal) {
		return _typeHandler[ordinal];
	} 

	@Override
	public final String getTableName() {
		return _tableName;
	}
	
	@Override
	public int getColumnCount() {
		return _columnCount;
	}
	
	@Override
	public final C getColumn(String columnName) {
		int pos = Arrays.binarySearch(_columnArraySortedStr, columnName);
		return pos < 0 ? null : _columnArraySorted[pos];
	}	

	@Override
	public final int getColumnOrdinal(String columnName) {
		return Arrays.binarySearch(_columnArraySortedStr, columnName);
	}	

	@Override
	public final C getColumn(int ordinal) {
		return _columnArray[ordinal];
	}

	@Override
	public Class<T> getEntityClass() {
		return _entityClass;
	}

	/* IQueryLiteral */
	
	@Override
	public final String getQueryText() {
		return _tableName;
	}
	
	@Override
	public final boolean isIdentifier() {
		return true;
	}

	@Override
	public String toString() {
		return _tableName;
	}

	public static class Simple<T,ID, C extends BaseColumnMeta> extends BaseEntityMeta<T, ID, C>{

		private String entityName;

		public Simple(int ordinal, String tableName, Class<T> entityClass){
			super(ordinal, tableName, entityClass);
			this.entityName = entityClass.getSimpleName();
		}
		
		@Override
		public String getEntityName() {
			return entityName;
		}

		@Override
		public boolean containsColumn(String columnName) {
			return false;
		}

		@Override
		public BaseColumnMeta<ID> getPrimaryColumn() {
			return null;
		}

		@Override
		public ID entityGetPrimary(T instance) {
			return null;
		}

		@Override
		public IUpdatable mutableCopy(Object v) {
			return null;
		}

		@Override
		public List<BaseColumnMeta> getColumns() {
			return null;
		}

		@Override
		public T fromResultSet(ResultSet rs) throws SQLException {
			return null;
		}
		
	}
	
	
}
