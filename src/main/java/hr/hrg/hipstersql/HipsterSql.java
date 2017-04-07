package hr.hrg.hipstersql;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.hrg.hipstersql.setter.BooleanSetter;
import hr.hrg.hipstersql.setter.DateSetter;
import hr.hrg.hipstersql.setter.DateTimeSetter;
import hr.hrg.hipstersql.setter.DoubleSetter;
import hr.hrg.hipstersql.setter.FloatSetter;
import hr.hrg.hipstersql.setter.IntegerSetter;
import hr.hrg.hipstersql.setter.LocalDateSetter;
import hr.hrg.hipstersql.setter.LocalTimeSetter;
import hr.hrg.hipstersql.setter.LongSetter;
import hr.hrg.hipstersql.setter.PreparedSetter;
import hr.hrg.hipstersql.setter.SqlDateSetter;
import hr.hrg.hipstersql.setter.StringSetter;

public class HipsterSql {
	
	public static final String ALL_ROWS = "ALL_ROWS";
	
	static Logger log = LoggerFactory.getLogger(HipsterSql.class);

	
	protected Pattern tableNamePattern = Pattern.compile("^[a-z_][a-z0-9_]+$");
	protected Pattern columnNamePattern = Pattern.compile("^[a-z_][a-z0-9_]+$");
	protected Set<String> allowdSqlOperators = new HashSet<>();  
	
	private static boolean yodaPresent = false;
	static {
		try {
			Class.forName("org.joda.time.DateTime");
			yodaPresent = true;
		} catch (ClassNotFoundException e) {
			log.warn("Joda DateTime classes not present, yoda DateTime support will be skipped");
		}
	}

    protected HashMap<Class<? extends Object>, PreparedSetter<? extends Object>> setters = new HashMap<>(); 

	protected String columQuote1 = "\"";
	protected String columQuote2 = "\"";

	public HipsterSql(){
		this.intAllowdOperators();

		addStatementSetter(Boolean.class, new BooleanSetter());
		addStatementSetter(Integer.class, new IntegerSetter());
		addStatementSetter(Long.class, new LongSetter());
		addStatementSetter(String.class, new StringSetter());

		addStatementSetter(Float.class, new FloatSetter());
		addStatementSetter(Double.class, new DoubleSetter());

		addStatementSetter(java.util.Date.class, new DateSetter());
		addStatementSetter(java.sql.Date.class, new SqlDateSetter());
		
		if(yodaPresent) {
			addStatementSetter(DateTime.class, new DateTimeSetter());
			addStatementSetter(LocalTime.class, new LocalTimeSetter());
			addStatementSetter(LocalDate.class, new LocalDateSetter());
		}
	}

	protected void intAllowdOperators() {
		addAllowdOperator("=","!=","<>","<",">",">=","<=","NOT","LIKE","ILIKE","IN");
	}
	protected void addAllowdOperator(String ...operators ){
		for(String operator: operators){
			allowdSqlOperators.add(operator.toUpperCase());
		}
	}

	public <T> void addStatementSetter(Class<T> clazz, PreparedSetter<T> setter){
		setters.put(clazz, setter);
	}
	
	public static boolean isYodaPresent() {
		return yodaPresent;
	}
	
	/** Override this method to provide primary column for a table. One useful thing with this
	 * is for generating insert statement in postgres that return newly inserted id. (using "RETURNING" keyword).*/
	public String getPrimary(String tableName) {
		return "id";
	}

	
	public boolean validateTableName(String name){
		return tableNamePattern.matcher(name).matches();		
	}
	
	public boolean validateColumnName(String name){
		return columnNamePattern.matcher(name).matches();
	}
	
	public boolean validateOperatorName(String name){
		return allowdSqlOperators.contains(name.toUpperCase());
	}
	
	/** generate query string for table name and quote if needed. Throws Exception if table name is invalid*/
	public String q_table(String name){
		name = name.trim().toLowerCase();
		if(!validateTableName(name)) throw new RuntimeException("Invalid table name "+name);
		return name;
	}

	/** generate query string for table name and quote if needed. Throws Exception if table name is invalid*/
	public String q_column(String name){
		name = name.trim().toLowerCase();
		if(!validateColumnName(name)) throw new RuntimeException("Invalid column name "+name);
		return name;
	}

	/** chack query string for query operator. Throws Exception if table name is invalid*/
	public String q_op(String name){
		name = name.trim().toLowerCase();
		if(!validateColumnName(name)) throw new RuntimeException("Invalid operator "+name);
		return name;
	}
	
	// ********************** UTILITY QUERY Functions **************************

	public Query buildFilter(List<?> params){
		int count = params.size();
		if(count == 0) return new Query();

		if(count == 1 && params.get(1) instanceof List) return buildFilter( (List<?>) params.get(0));
		
		Object p0 = params.get(0);
		if(count == 1 && p0 instanceof List) return buildFilter( (List<?>) p0);
		
		Object p1 = params.get(1);
		if(count == 2) {
			if(p1 == null) return new Query(q_column((String) p0)+"IS NULL");
			return new Query(q_column((String) p0)+" = ",p1);
		}

		if(count == 3) {
			String operator = (String) p1;
			Object p2 = params.get(2);
			if(p2 == null) {
				if("=".equals(operator)) {
					return new Query(q_column((String) p0)+" IS NULL");
				}else if("!=".equals(operator) || "<>".equals(operator)) {					
					return new Query(q_column((String) p0)+" IS NOT NULL");
				}
			}
			return new Query(q_column((String) p0)+" "+q_op(operator)+" ",p2);
		}
		
		throw new RuntimeException("buildFilter only accepts 2 or 3 paramteres");
	}
	
	public Query buildFilter(Object ...params){		
		int count = params.length;
		if(count == 0) return new Query();
		
		Object p0 = params[0];
		if(count == 1 && p0 instanceof List) return buildFilter( (List<?>) p0);
		
		Object p1 = params[1];
		if(count == 2) {
			if(p1 == null) return new Query(q_column((String) p0)+" IS NULL");
			return new Query(q_column((String) p0)+" = ",p1);
		}

		if(count == 3) {
			String operator = (String) p1;
			Object p2 = params[2];
			if(p2 == null) {
				if("=".equals(operator)) {
					return new Query(q_column((String) p0)+" IS NULL");
				}else if("!=".equals(operator) || "<>".equals(operator)) {					
					return new Query(q_column((String) p0)+" IS NOT NULL");
				}
			}
			return new Query(q_column((String) p0)+" "+q_op(operator)+" ",p2);
		}
		throw new RuntimeException("buildFilter only accepts 2 or 3 paramteres");
	}

	/** build insert Query from map of key->value. Although the map is not declared as Map<String,Object> 
	 * keys must be strings or ClassCastException will be thrown. */
	public Query buildInsert(String tableName, Map<?,?> values) {
		StringBuilder firstPart = new StringBuilder("INSERT INTO ").append(q_table(tableName)).append("("); 

		Query valuesPart = new Query(" VALUES(");
		
		int i=0;
		for(Entry<?, ?> entry:values.entrySet()) {
			if(i >0) {
				firstPart.append(",");
				valuesPart.append(",",entry.getValue());
			}else {
				valuesPart.appendValue(entry.getValue());
			}
			firstPart.append(q_column((String) entry.getKey()));
			i++;
		}
		valuesPart.append(")");
		
		return new Query(firstPart, valuesPart);		
	}

	/** build insert Query from vararg paramteters that are pairs (column,value) . Although method accepts objects 
	 * keys must be strings or ClassCastException will be thrown. */
	public Query buildInsertVar(String tableName, Object ...values){
		StringBuilder firstPart = new StringBuilder("INSERT INTO ").append(q_table(tableName)).append("("); 

		Query valuesPart = new Query(")VALUES(");
		
		for(int i=1; i<values.length; i+=2){
			System.out.println(values[i-1]+"="+values[i]);
			if(i >1) {
				firstPart.append(",");
				valuesPart.append(",",values[i]);
			}else {
				valuesPart.appendValue(values[i]);
			}
			firstPart.append(q_column((String) values[i-1]));
		}
		valuesPart.append(")");

		return new Query(firstPart, valuesPart);		
	}

	/** Build update Query from map of column->value. Although the map is not declared as Map<String,Object> 
	 * keys must be strings or ClassCastException will be thrown. 
	 * 
	 * @param tableName
	 * @param filter filter to define which rows to update (be careful not to update the whole table) see: {@link #checkFilterDefined(Object)}
	 * @param values column->value pairs to generate instead of writing manually<br> 
	 * <code>new Query("UPDATE [tableName] SET col1=", val1, ",col2=", val2, " WHERE [filter]")</code>
	 * @return generated query object
	 */
	public Query buildUpdate(String tableName, Object filter, Map<?,?> values){
		Query filterQuery = checkFilterDefined(filter);

		Query query = new Query("UPDATE "+q_table(tableName)+" SET ");

		int i=0;
		for(Entry<?, ?> entry:values.entrySet()) {
			if(i >0){
				query.append(","+q_column((String) entry.getKey())+"=", entry.getValue());
			}else {
				query.append(q_column((String) entry.getKey())+"=", entry.getValue());
			}
			i++;
		}

		if(filterQuery != null) query.append(" WHERE ", filterQuery);

		return query;		
	}
	
	/** build update Query from  column->value pairs supplied as varargs. Although the map is not declared as Map<String,Object> 
	 * keys must be strings or ClassCastException will be thrown. 
	 * @param tableName
	 * @param filter filter to define which rows to update (be careful not to update the whole table) see: {@link #checkFilterDefined(Object)}
	 * @param values column-value pairs to generate instead of writing manually<br> 
	 * <code>new Query("UPDATE [tableName] SET col1=", val1, ",col2=", val2, " WHERE [filter]")</code>
	 * @return generated query object
	 * */
	public Query buildUpdateVar(String tableName, Object filter, Object ...values){
		Query filterQuery = checkFilterDefined(filter);

		Query query = new Query("UPDATE "+q_table(tableName)+" SET ");

		for(int i=1; i<values.length; i+=2){
			if(i >1){
				query.append(","+q_column((String) values[i-1])+"=", values[i]);
			}else {
				query.append(q_column((String) values[i-1])+"=", values[i]);
			}
		}

		if(filterQuery != null) query.append(" WHERE ", filterQuery);

		return query;		
	}

	/** Check if the object is either {@link #ALL_ROWS} or a non-empty query. You can also use new Query("1=1"). <br>
	 * Helps to sometimes catch unintended whole table update when used by {@link #buildUpdate(String, Object, Map)} or {@link #buildUpdateVar(String, Object, Object...)}
	 * 
	 * @param filter
	 * @return the same filter provided s parameter to allow inline use
	 * @throws RuntimeException if filter is null or empty query
	 */
	public Query checkFilterDefined(Object filter) {
		if(ALL_ROWS.equals(filter)) return null;// ok, in this case use the empty query to avoid any filtering
		
		if(filter == null || !(filter instanceof Query) || ((Query)filter).isEmpty()){				
			throw new RuntimeException("Non empty filter query must be supplied to limit the number of rows affected to avoid accidental full table updates. To force update without filter use HipsterSql.ALL_ROWS");
		}
		return (Query) filter;
	}

	public PreparedQuery prepare(Object ... queryParts){
		StringBuilder b = new StringBuilder();
		List<Object> params = new ArrayList<>();		

		Query query;
		if(queryParts.length == 1 && queryParts[0] instanceof Query){
			query = (Query) queryParts[0];
		}else{
			query = new Query(queryParts);
		}
		
		List<Object> flat = query.getFlatten();
		int count = flat.size();
		
		for(int i=0; i<count; i++){
			if(i%2 == 0){
				b.append(flat.get(i));
			}else {
				b.append('?');
				params.add(flat.get(i));
			}
		}

		return new PreparedQuery(b.toString(), params);
		
	}

	/** Set a value into the prepared statement <br>
	 * <br>
	 * Override if using {@link PreparedValue} or defining {@link PreparedSetter} is not sufficient to provide functionality.  
	 * 
	 * @param hipConnection connection that produced the PreparedStatement
	 * @param ps the statement
	 * @param i index in the statement
	 * @param value value to set
	 * @throws SQLException
	 */
	@SuppressWarnings({ "unchecked"})
	public <C> void prepSet(HipsterConnection hipConnection,PreparedStatement ps, int i, C value) throws SQLException {
		if(value == null){
			ps.setNull(i, Types.OTHER);
			return;
		}

		if(value instanceof PreparedValue) {
			((PreparedValue)value).set(ps, i);
		}

		PreparedSetter<C> setter = (PreparedSetter<C>) setters.get(value.getClass());
		
		if(setter == null){
			throw new RuntimeException("Setter not defined for type "+value.getClass()+" in prepared statement: "+hipConnection.getLastPrepared().getQueryString()+" on index "+i+" using value "+value);
		}
		
		setter.set(ps, i, (C)value);

	}

	/** HipsterSql returns DateTime LocalDate and LocalTime from yoda-time library if it is present in the classpath.<br>
	 * Otherwise returns java.sql(Date,Time,TimeStamp).<br>
	 * <br>
	 * Override to customise for yourself.
	 * 
	 * @param rs ResultSet
	 * @param index for value
	 * @param sqlType for the value
	 * @param column name
	 * @return extracted Object presenting the time from database
	 * @throws SQLException
	 */
	private Object handleRsGetTime(ResultSet rs, int index, int sqlType, String column) throws SQLException {
		if(yodaPresent) {			
			switch (sqlType) {
			case Types.DATE: Date d = rs.getDate(index); return d == null ? null: new LocalDate(d);
			case Types.TIME: Time t = rs.getTime(index); return t == null ? null: new LocalTime(t);
			case Types.TIMESTAMP: Timestamp dt = rs.getTimestamp(index); return dt == null ? null: new DateTime(dt);
			}
		}else{
			switch (sqlType) {
			case Types.DATE: return rs.getDate(index);
			case Types.TIME: return rs.getTime(index);
			case Types.TIMESTAMP: return rs.getTimestamp(index);
			}
		}
		return null;
	}

	private Object handleRsGetOther(HipsterConnection hipcConnection, ResultSet rs, int index, int sqlType, String column) throws SQLException {
        log.warn("unhandled sql type "+sqlType+", at index "+index+", column: "+column+" query: "+hipcConnection.getLastQuery());
        return rs.getString(index);
	}

	Object handleRsGet(HipsterConnection hipcConnection, ResultSet rs, int index, int sqlType, String column) throws SQLException {
    	
        switch (sqlType) {
            case Types.INTEGER: return Integer.valueOf(rs.getInt(index));
            case Types.BIGINT: return Long.valueOf(rs.getLong(index));
            case Types.SMALLINT: return Integer.valueOf(rs.getInt(index));
            case Types.FLOAT: return Float.valueOf(rs.getFloat(index));
            case Types.DOUBLE: return Double.valueOf(rs.getDouble(index));

            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP: return handleRsGetTime(rs, index, sqlType, column);
            
            case Types.CHAR: ;
            case Types.VARCHAR: return rs.getString(index);
            default: return handleRsGetOther(hipcConnection, rs, index, sqlType, column);
        }
    }

	/* **************************************     UTILITIES FOR PRINTING QUERY TO STRING FOR LOGG AND DEBUG **************************/
	
	public static String escape(String str){
		return str.replace("'", "''").replace("\\", "\\\\");
	}

	public static StringBuilder escape(StringBuilder b, String str){
		b.append(str.replace("'", "''").replace("\\", "\\\\"));
		return b;
	}

	public static String quote(String str){
		return quote(new StringBuilder(),str).toString();
	}

	public static StringBuilder quote(StringBuilder b, String str){
		b.append('\'').append(str).append('\'');
		return b;
	}
	
	public static StringBuilder qValue(StringBuilder b, Object val) {
		if(val == null){
			b.append("NULL");
		}else if(val instanceof Number){
			b.append(val);
		}else if(val instanceof Boolean){
			b.append(((Boolean)val).booleanValue() ? "true":"false");
		}else{
			quote(b,escape(val.toString()));
		}
		return b;
	}	
	
}
