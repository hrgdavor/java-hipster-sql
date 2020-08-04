package hr.hrg.hipster.entity;

import java.sql.*;
import java.util.*;

import hr.hrg.hipster.sql.*;
import hr.hrg.hipster.type.*;

@SuppressWarnings("rawtypes")
public abstract class EntityMeta<T,ID, C extends ColumnMeta, V> implements IEntityMeta<T,ID>{

	public static final String ERR_ENTITY = "Problem reading entity data for ";
	
	protected final int _ordinal;
	protected final String _tableName;
	protected final Class<T> _entityClass;
    
	protected ImmutableList<C> _columns;
	protected ICustomType<?>[] _typeHandler;
	protected int _columnCount;
	protected C[] _columnArray;
	
	protected C[] _columnArraySorted;
	protected String[] _columnArraySortedStr;

	protected C[] _fieldArraySorted;
	protected String[] _fieldArraySortedStr;

	protected HipsterSql _hip;

	public EntityMeta(int ordinal, String tableName, Class<T> entityClass, HipsterSql hipster) {
		this._ordinal = ordinal;
		this._tableName = tableName;
		this._entityClass = entityClass;
		this._hip = hipster;
	}

	public EntityMeta(int ordinal, String tableName, Class<T> entityClass, C[] columnArray, String[] columnArraySortedStr, C[] columnArraySorted, String[] fieldArraySortedStr, C[] fieldArraySorted, HipsterSql hipster) {
		this(ordinal, tableName, entityClass, hipster);

		this._columnArraySorted = columnArraySorted;
		this._columnArraySortedStr = columnArraySortedStr;
		this._fieldArraySorted = fieldArraySorted;
		this._fieldArraySortedStr = fieldArraySortedStr;
		this._columnCount = columnArray.length;
		_typeHandler = new ICustomType<?>[_columnCount];
	}
	
	public static RuntimeException errEntity(Object id, Class<?> entity, Throwable cause){
		return new RuntimeException(ERR_ENTITY+entity.getName()+"#"+id,cause);
	}
	
	@Override
	public final int ordinal() {
		return _ordinal;
	}

	@Override
	public final ICustomType<?> getTypeHandler(ColumnMeta column) {
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
	public final C getField(String columnName) {
		int pos = Arrays.binarySearch(_fieldArraySortedStr, columnName);
		return pos < 0 ? null : _fieldArraySorted[pos];
	}	

	@Override
	public final int getFieldOrdinal(String columnName) {
		return Arrays.binarySearch(_fieldArraySortedStr, columnName);
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

	public ImmutableList<C> getColumns() {
		return _columns;
	}

	public void visitResults(IHipsterConnection hc, Object sql, V visitor) {

		Boolean autoCommit = null;
		try {
			// postgres does not use cursor if autoCommit is on
			autoCommit = hc.getSqlConnection().getAutoCommit();
			hc.getSqlConnection().setAutoCommit(false);
		} catch (SQLException e) {
			throw new HipsterSqlException(hc, "autoCommit", e);
		}

		try (Result res = new Result(hc);) {
			res.setFetchSize(512);

			res.executeQuery(sql);

			while (res.next()) {
				visitResult(res.getResultSet(), visitor);
			}
		} catch (Exception e) {
			throw new HipsterSqlException(hc, "visit failed", e);
		} finally {
			try {
				if(autoCommit != null) hc.getSqlConnection().setAutoCommit(autoCommit);
			} catch (SQLException e) {
				throw new HipsterSqlException(hc, "autoCommit", e);
			}
		}
	}

	public void visitResult(ResultSet rs, V visitor) throws SQLException{
		throw new UnsupportedOperationException();
	}
	
	public T fromResultSet(ResultSet rs) throws SQLException{
		throw new UnsupportedOperationException();
	}	
	
}
