package hr.hrg.hipster.sql;

import static hr.hrg.hipster.query.QueryLiteral.*;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.Map.*;
import java.util.regex.*;

import org.joda.time.*;
import org.slf4j.*;

import hr.hrg.hipster.change.*;
import hr.hrg.hipster.entity.*;
import hr.hrg.hipster.query.*;
import hr.hrg.hipster.type.*;

public class HipsterSql {
	
	public static final String ALL_ROWS = "ALL_ROWS";
	static Logger log = HipsterSqlUtil.isSlf4jApiPresent() ? LoggerFactory.getLogger(HipsterSql.class) : null;
	
	protected Pattern tableNamePattern = Pattern.compile("^[a-z_][a-z0-9_]+$");
	protected Pattern columnNamePattern = Pattern.compile("^[a-z_][a-z0-9_]+$");
	protected Set<String> allowdSqlOperators = new HashSet<>();  

	protected String columQuote1 = "\"";
	protected String columQuote2 = "\"";

	private TypeSource typeSource;
	private EntitySource entitySource;
	
	private EntityEventHub eventHub;
	private IQueryLogger queryLogger;

	public HipsterSql() {
		this.typeSource = new TypeSource();
		this.entitySource = new EntitySource(this);
		this.eventHub = new EntityEventHub(entitySource);
		this.intAllowdOperators();
	}

	protected void intAllowdOperators() {
		addAllowdOperator("=","!=","<>","<",">",">=","<=","NOT","LIKE","ILIKE","IN");
	}

	protected void addAllowdOperator(String ...operators ){
		for(String operator: operators){
			allowdSqlOperators.add(operator.toUpperCase());
		}
	}
		
	public EntitySource getEntitySource() {
		return entitySource;
	}
	
	public TypeSource getTypeSource() {
		return typeSource;
	}
	
	public EntityEventHub getEventHub() {
		return eventHub;
	}

	public IHipsterConnection openConnection(String url) throws SQLException {
		Connection connection = DriverManager.getConnection(url);
		return new HipsterConnectionImpl(this, connection);
	}
	
	public IHipsterConnection openConnection(String url, String username, String password) throws SQLException {
		Connection connection = DriverManager.getConnection(url,username, password);
		return new HipsterConnectionImpl(this, connection);
	}
	
	public IHipsterConnection openConnection(String url, Properties conf) throws SQLException {
		Connection connection = DriverManager.getConnection(url,conf);
		return new HipsterConnectionImpl(this, connection);
	}
	
	public IHipsterConnection openConnection(Connection sqlConnection) {
		return new HipsterConnectionImpl(this, sqlConnection);
	}
	
	/** Override this method to provide primary column for a _table. One useful thing with this
	 * is for generating insert statement in postgres that return newly inserted id. (using "RETURNING" keyword).
	 * @param tableName table name
	 * @return name of the primary column
	 * */
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
	
	/** generate query string for _table name and quote if needed. Throws Exception if _table name is invalid
	 * @param name _table name
	 * @return sanitised _table name
	 * */
	public String q_table(String name){
		name = name.trim().toLowerCase();
		if(!validateTableName(name)) throw new RuntimeException("Invalid _table name "+name);
		return name;
	}

	/** generate query string for _table name and quote if needed. Throws Exception if column name is invalid
	 * @param name column name
	 * @return sanitised column name
	 * */
	public String q_column(String name){
		name = name.trim().toLowerCase();
		if(!validateColumnName(name)) throw new RuntimeException("Invalid column name "+name);
		return name;
	}

	/** check query string for query operator. Throws Exception if operator name is invalid.
	 * 
	 * @param name operator name
	 * @return operator name
	 * */
	public String q_op(String name){
		name = name.trim().toLowerCase();
		if(!validateColumnName(name)) throw new RuntimeException("Invalid operator "+name);
		return name;
	}
	
	// ********************** UTILITY QUERY Functions **************************

	public Query buildFilter(List<?> params){
		int count = params.size();
		if(count == 0) return this.q();

		if(count == 1 && params.get(1) instanceof List) return buildFilter( (List<?>) params.get(0));
		
		Object p0 = params.get(0);
		if(count == 1 && p0 instanceof List) return buildFilter( (List<?>) p0);
		
		Object p1 = params.get(1);
		if(count == 2) {
			if(p1 == null) return this.q(identifier((String) p0)," IS NULL");
			return this.q(identifier((String) p0)," = ",p1);
		}

		if(count == 3) {
			String operator = (String) p1;
			Object p2 = params.get(2);
			if(p2 == null) {
				if("=".equals(operator)) {
					return this.q(q_column((String) p0)," IS NULL");
				}else if("!=".equals(operator) || "<>".equals(operator)) {					
					return this.q(q_column((String) p0)+" IS NOT NULL");
				}
			}
			return this.q(identifier((String) p0)," "+q_op(operator)+" ",p2);
		}

		throw new RuntimeException("buildFilter only accepts 2 or 3 paramteres");
	}
	
	public Query buildFilter(Object ...params){		
		int count = params.length;
		if(count == 0) return this.q();
		
		Object p0 = params[0];
		if(count == 1 && p0 instanceof List) return buildFilter( (List<?>) p0);
		
		Object p1 = params[1];
		if(count == 2) {
			if(p1 == null) return this.q(identifier((String) p0)," IS NULL");
			return this.q(identifier((String) p0)," = ",p1);
		}

		if(count == 3) {
			String operator = (String) p1;
			Object p2 = params[2];
			if(p2 == null) {
				if("=".equals(operator)) {
					return this.q(identifier((String) p0)," IS NULL");
				}else if("!=".equals(operator) || "<>".equals(operator)) {					
					return this.q(identifier((String) p0)," IS NOT NULL");
				}
			}
			return this.q(identifier((String) p0)," "+q_op(operator)+" ",p2);
		}
		throw new RuntimeException("buildFilter only accepts 2 or 3 paramteres");
	}

	/** Build insert query from an object, with help of entity meta-data. Columns that were not changed
	 * will be omitted from query.
	 * 
	 * @param meta entity meta-data
	 * @param mutable object with updated values
	 * @return query ready for inserting
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Query buildInsert(IEntityMeta<?, ?> meta, IUpdatable mutable) {
		
		if(!meta.getEntityClass().isAssignableFrom(mutable.getClass())) 
			throw new RuntimeException("Meta class missmatch "+meta.getClass()+"("+meta.getEntityClass().getName()+") is not suitable for "+mutable.getClass().getName());

		StringBuilder firstPart = new StringBuilder("("); 
		Query valuesPart = this.q(")VALUES(");
		
		int i=0;
		int ordinal;
		for(ColumnMeta column:meta.getColumns()) {
			ordinal = column.ordinal();			
			
			if(!mutable.isChanged(ordinal)) continue;
			
			Object value = mutable.getValue(column);
			ICustomType<?> customType = meta.getTypeHandler(column);
			if(i >0) {
				firstPart.append(",");
				valuesPart.addParts(",",new QueryValue(value, customType));
			}else {
				valuesPart.addValue(new QueryValue(value, customType));
			}
			firstPart.append(column);
			i++;
		}
		valuesPart.add(")");
		
		return this.q("INSERT INTO ", meta, firstPart, valuesPart);		
	}
	
	/** build insert QueryOld from map of key:value. Although the map is not declared as Map&lt;String,Object&gt; 
	 * keys must be strings or ClassCastException will be thrown. 
	 * 
	 * @param tableName table name
	 * @param values map with values to insert
	 * @return generated query object
	 * */
	public Query buildInsert(String tableName, Map<?,?> values) {
		StringBuilder firstPart = new StringBuilder("INSERT INTO ").append(q_table(tableName)).append("("); 

		Query valuesPart = this.q(")VALUES(");
		
		int i=0;
		for(Entry<?, ?> entry:values.entrySet()) {
			if(i >0) {
				firstPart.append(",");
				valuesPart.addParts(",",entry.getValue());
			}else {
				valuesPart.addValue(entry.getValue());
			}
			firstPart.append(q_column((String) entry.getKey()));
			i++;
		}
		valuesPart.add(")");
		
		firstPart.append(valuesPart.getQueryExpression());
		return new Query(this, firstPart, valuesPart.getSize(), valuesPart.getValues());
	}

	/** build insert QueryOld from vararg paramteters that are pairs (column,value) . Although method accepts objects 
	 * keys must be strings or ClassCastException will be thrown. 
	 * 
	 * @param tableName _table name
	 * @param values map expressed as key:value pairs (for values to insert)
	 * @return generated query object
	 * */
	public Query buildInsertVar(String tableName, Object ...values){
		StringBuilder firstPart = new StringBuilder("INSERT INTO ").append(q_table(tableName)).append("("); 

		Query valuesPart = this.q(")VALUES(");
		
		for(int i=1; i<values.length; i+=2){
			if(i >1) {
				firstPart.append(",");
				valuesPart.addParts(",",values[i]);
			}else {
				valuesPart.addValue(values[i]);
			}
			firstPart.append(q_column((String) values[i-1]));
		}
		valuesPart.add(")");

		firstPart.append(valuesPart.getQueryExpression());
		return new Query(this, firstPart, valuesPart.getSize(), valuesPart.getValues());		
	}

	/** Build update query from an object, with help of entity metadata. Columns that were not changed
	 * will be omitted from query.
	 * 
	 * @param meta entity meta-data
	 * @param filter filter
	 * @param mutable object with updated values
	 * @return query ready for inserting
	 */
	@SuppressWarnings("rawtypes")
	public Query buildUpdate(IEntityMeta<?, ?> meta, Object filter, IUpdatable mutable) {
		
		if(!meta.getEntityClass().isAssignableFrom(mutable.getClass())) 
			throw new RuntimeException("Meta class missmatch "+meta.getClass()+"("+meta.getEntityClass().getName()+") is not suitable for "+mutable.getClass().getName());

		Query filterQuery = checkFilterDefined(filter);
		
		Query query = this.q("UPDATE ",meta," SET ");

		int i=0;
		int ordinal;
		for(ColumnMeta column:meta.getColumns()) {
			ordinal = column.ordinal();
			
			if(!mutable.isChanged(ordinal)) continue;
			
			Object value = mutable.getValue(ordinal);
			ICustomType<?> customType = meta.getTypeHandler(ordinal);
			if(i >0){
				query.addParts(",",column,"=", new QueryValue(value, customType));
			}else {
				query.addParts(column,"=", new QueryValue(value, customType));
			}
			i++;
		}

		if(filterQuery != null) query.addParts(" WHERE ", filterQuery);

		return query;	
	}
	
	
	/** Build update QueryOld from map of column:value. Although the map is not declared as Map&lt;String,Object&gt; 
	 * keys must be strings or ClassCastException will be thrown. 
	 * 
	 * @param tableName table name
	 * @param filter filter to define which rows to update (be careful not to update the whole _table) see: {@link #checkFilterDefined(Object)}
	 * @param values column:value pairs to generate instead of writing manually<br> 
	 * <code>new QueryOld("UPDATE [_tableName] SET col1=", val1, ",col2=", val2, " WHERE [filter]")</code>
	 * @return generated query object
	 */
	public Query buildUpdate(String tableName, Object filter, Map<?,?> values){
		Query filterQuery = checkFilterDefined(filter);

		Query query = this.q("UPDATE ",identifier(tableName)," SET ");

		int i=0;
		for(Entry<?, ?> entry:values.entrySet()) {
			if(i >0){
				query.addParts(",",identifier((String) entry.getKey()),"=", entry.getValue());
			}else {
				query.addParts(identifier((String) entry.getKey()),"=", entry.getValue());
			}
			i++;
		}

		if(filterQuery != null) query.addParts(" WHERE ", filterQuery);

		return query;		
	}
	
	/** build update QueryOld from  column:value pairs supplied as varargs. Although the map is not declared as Map&lt;String,Object&gt; 
	 * keys must be strings or ClassCastException will be thrown. 
	 * 
	 * @param tableName _table name
	 * @param filter filter to define which rows to update (be careful not to update the whole _table) see: {@link #checkFilterDefined(Object)}
	 * @param values column-value pairs to generate instead of writing manually<br> 
	 * <code>new QueryOld("UPDATE [_tableName] SET col1=", val1, ",col2=", val2, " WHERE [filter]")</code>
	 * @return generated query object
	 * */
	public Query buildUpdateVar(String tableName, Object filter, Object ...values){
		Query filterQuery = checkFilterDefined(filter);

		Query query = this.q("UPDATE "+identifier(tableName)+" SET ");

		for(int i=1; i<values.length; i+=2){
			if(i >1){
				query.addParts(","+identifier((String) values[i-1])+"=", values[i]);
			}else {
				query.addParts(identifier((String) values[i-1])+"=", values[i]);
			}
		}

		if(filterQuery != null) query.addParts(" WHERE ", filterQuery);

		return query;
	}

	/** Check if the object is either {@link #ALL_ROWS} or a non-empty query. You can also use new QueryOld("1=1"). <br>
	 * Helps to sometimes catch unintended whole _table update when used by {@link #buildUpdate(String, Object, Map)} or {@link #buildUpdateVar(String, Object, Object...)}
	 * 
	 * @param filter filter
	 * @return the same filter provided s parameter to allow inline use
	 * @throws RuntimeException if filter is null or empty query
	 */
	public Query checkFilterDefined(Object filter) {
		if(ALL_ROWS.equals(filter)) return null;// ok, in this case use the empty query to avoid any filtering
		
		if(filter == null || !(filter instanceof Query) || ((Query)filter).isEmpty()){
			throw new RuntimeException("Non empty filter query must be supplied to limit the number of rows affected to avoid accidental full _table updates. To force update without filter use HipsterSql.ALL_ROWS");
		}
		return (Query) filter;
	}

//	/** Convert {@link QueryOld} object into a {@link PreparedQuery}
//	 * 
//	 * @param queryParts query parts
//	 * @return PreparedQuery that is ready for execution
//	 */
//	public PreparedQuery prepare(Object ... queryParts){
//		if(queryParts.length == 1 && queryParts[0] instanceof PreparedQuery){
//			return (PreparedQuery) queryParts[0];
//		}
//		
//		StringBuilder b = new StringBuilder();
//		ArrayList<Object> values = new ArrayList<>();
//		List<ICustomType<?>> setters = new ArrayList<>();
//		
//		prepareInto(b, values, setters, queryParts);
//		
//		return new PreparedQuery(typeSource,b, values, setters); 
//	}
//	
//	public void prepareInto(StringBuilder b, List<Object> values, List<ICustomType<?>> setters, Object ... queryParts){
//		
//		int count = queryParts.length;
//		
//		boolean expectValue = false;
//		
//		for(int i=0; i<count; i++){
//			Object obj = queryParts[i];
//		
//			if(obj instanceof IQueryLiteral){
//				IQueryLiteral queryLiteral = (IQueryLiteral)obj;
//				if(queryLiteral.isIdentifier()) b.append(columQuote1);
//				b.append(queryLiteral.getQueryText());
//				if(queryLiteral.isIdentifier()) b.append(columQuote2);
//				
//				expectValue = false;
//				
//			}else if(obj instanceof ColumnMeta){
//				b.append(columQuote1).append(((ColumnMeta)obj).getColumnName()).append(columQuote2);
//				
//				expectValue = false;
//
//			}else if(obj instanceof QueryOld){
//				prepareInto(b, values, setters, ((QueryOld)obj).getParts().toArray());
//				
//				expectValue = false;
//				
//			}else if(obj instanceof PreparedQuery){
//				PreparedQuery prepared = (PreparedQuery) obj;
//				b.append(prepared.getQueryStringBuilder());
//				values.addAll(prepared.getParams());
//			
//				expectValue = false;
//				
//			}else if(obj instanceof ICustomType){
//				// prepare CustomType for the next parameter
//				PreparedQuery.setCustom(setters, values.size(), (ICustomType) obj);
//				
//				expectValue = true; // we just defined custom type for the next value, so yeah :) value expected
//				
//			}else if(!expectValue){
//				b.append(obj);
//
//				expectValue = true; // we just appended a query part to the StringBuilder, so next must be a value
//
//			}else {
//				b.append('?');
//				values.add(obj);
//
//				expectValue = false;
//			}
//		}
//	}

	public String getColumQuote1() {
		return columQuote1;
	}
	
	public String getColumQuote2() {
		return columQuote2;
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
	 * @throws SQLException if get fails
	 */
	protected Object handleRsGetTime(ResultSet rs, int index, int sqlType, String column) throws SQLException {
		if(HipsterSqlUtil.isYodaPresent()) {			
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

	protected Object handleRsGetOther(IHipsterConnection hipcConnection, ResultSet rs, int index, int sqlType, String column) throws SQLException {
        if(log != null && log.isWarnEnabled()) log.warn("unhandled sql type "+sqlType+", at index "+index+", column: "+column+" query: "+hipcConnection.getLastQuery());
        return rs.getString(index);
	}

	protected Object handleRsGet(IHipsterConnection hipcConnection, ResultSet rs, int index, int sqlType, String column) throws SQLException {
    	
        switch (sqlType) {
            case Types.INTEGER: 
            	int retInt = rs.getInt(index); 
            	return rs.wasNull() ? null: Integer.valueOf(retInt);
            case Types.BIGINT:  
            	long retLong = rs.getLong(index); 
            	return rs.wasNull() ? null: Long.valueOf(retLong);
            case Types.SMALLINT: 
            	short retShort = rs.getShort(index);  
            	return rs.wasNull() ? null: Short.valueOf(retShort);
            case Types.FLOAT: 
            	float retFloat = rs.getFloat(index); 
            	return rs.wasNull() ? null: Float.valueOf(retFloat);
            case Types.DOUBLE: 
            	double retDouble = rs.getDouble(index); 
            	return rs.wasNull() ? null: Double.valueOf(retDouble);

            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP: return handleRsGetTime(rs, index, sqlType, column);
            
            case Types.CHAR: ;
            case Types.VARCHAR: return rs.getString(index);
            default: return handleRsGetOther(hipcConnection, rs, index, sqlType, column);
        }
    }

    /* (non-Javadoc)
	 * @see hr.hrg.hipster.sql.HipsterConnection#rowsLimit(int, int, java.lang.Object)
	 */
	public void appendLimit(int offset, int limit, StringBuilder queryExpressionBuilder){
    	queryExpressionBuilder
    		.append(" LIMIT ") .append(limit)
    		.append(" OFFSET ").append(offset);
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

	public Query qSelect(EntityMeta<?,?,?,?> meta) {
		return q("SELECT ").addColumns(meta).add(" FROM ", meta).add(' ');
	}

	public Query q(Object ...parts) {
		if(parts.length == 1 && parts[0] instanceof Query) return (Query) parts[0];
		
		// likely needed length is guessed as half parameters plus 1
		Query q = new Query(this, new StringBuilder(), (int)(parts.length/2) + 1);
		q.addParts(parts);
		return q;
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

	public IQueryLogger getQueryLogger() {
		return queryLogger;
	}
	
	public void setQueryLogger(IQueryLogger queryLogger) {
		this.queryLogger = queryLogger;
	}
}
