package hr.hrg.hipster.query;

import java.sql.*;

import hr.hrg.hipster.type.*;

public final class QueryValue<T> implements IQueryValue{
	final T value;
	final ICustomType<T> type;

	public QueryValue(T value, ICustomType<T> type){
		if(type == null) throw new NullPointerException("EntityEventType can not be null");
		this.value = value;
		this.type = type;
	}

	@Override
	public  void set(PreparedStatement ps, int index) throws SQLException {
		type.set(ps, index, value);
	}

	@Override
	public String toString() {
		return type.valueToQueryString(value);
	}
	
	public static QueryValueInt     v(int     value){ return new QueryValueInt(value); }
	public static QueryValueLong    v(long    value){ return new QueryValueLong(value); }
	public static QueryValueFloat   v(float   value){ return new QueryValueFloat(value); }
	public static QueryValueDouble  v(double  value){ return new QueryValueDouble(value); }
	public static QueryValueBoolean v(boolean value){ return new QueryValueBoolean(value); }
}