package hr.hrg.hipster.sql;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

import javax.persistence.*;

public class VisitorSource {

	protected Map<Class<? extends Object>, IResultVisitor<?>> registered = new ConcurrentHashMap<>(); 

	private ResultGetterSource resultGetterSource;

	public VisitorSource(ResultGetterSource resultGetterSource){
		this.resultGetterSource = resultGetterSource;
	}

	public ResultGetterSource getResultGetterSource() {
		return resultGetterSource;
	}

	public <T> void registerFor(IResultVisitor<?> handler, Class<T> clazz){
		registered.put(clazz, handler);
	}

	/** 
	 * @param clazz Class that is generated by {@link IReadMeta}
	 * @return {@link IReadMeta} or throw RuntimeException if none registered
	 */
	public <T> IResultVisitor<T> getForRequired(Class<T> clazz) {
		IResultVisitor<T> for1 = getFor(clazz);
		if(for1 == null) throw new RuntimeException("VisotorHandler not found for "+clazz.getName());
		return for1;
	}

	/** 
	 * @param clazz Class that is generated by {@link IReadMeta}
	 * @return {@link IReadMeta} or null if none registered
	 */
	@SuppressWarnings("unchecked")
	public <T> IResultVisitor<T> getFor(Class<T> clazz) {
		return (IResultVisitor<T>) registered.get(clazz);
	}
	
	/** Create a read-only proxy version if no {@link IReadMeta} is registered yet
	 * 
	 * @param clazz Class that is generated by {@link IReadMeta}
	 * @return {@link IReadMeta}
	 */
	@SuppressWarnings("unchecked")
	public <T> IResultVisitor<T> getOrCreate(Class<T> clazz) {
		IResultVisitor<T> ret = (IResultVisitor<T>) registered.get(clazz);
		if(ret == null){
			// do not care about concurrency here, if two threads cause creation the resulting ReaderSource will be the same
			// so it is not important which version ends-up in the map
			if(!clazz.isInterface()) throw new RuntimeException("Only interfaces are supported for generating Iresultvisitor ");
			
			ret = newResultVisitorHandler(clazz);
			registered.put(clazz, ret);
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	<T> IResultVisitor<T> newResultVisitorHandler(final Class<T> clazz) {

//		throw new RuntimeException("not implemented");

		// V1 use same naming convention as hipster-processor and try to load that class
		// hipster-processor will add "Handler" to the visitor interface name
		String cName = HipsterSqlUtil.entityNamesPrefix(clazz)+"Handler";
		IResultVisitor<T> handler = loadHandlerFromClass(cName);
		if(handler != null) return handler;

		cName = clazz.getName();
		// V2: use same naming convention as hipster-processor and try to load that class
		// hipster-processor will add IResultVisitor to the Meta class
		if(cName.endsWith("Visitor")){
			cName = cName.substring(cName.length()-7)+"Meta";
			// if this interface is generated from an entity Interface, then the meta from original entity
			// will implement IResultVisitor. 
			// mypackage.MyEntity -> mypackage.MyEntityVisitor 
			// mypackage.MyEntity -> mypackage.MyEntityMeta
			
			handler = loadHandlerFromClass(cName);
			if(handler != null) return handler;
		}
		
		// no Visitor handler generated by hipster-processor, create one that returns results by calling the lambda method
		Method[] methods = clazz.getMethods();
		
		if(methods.length != 1) throw new RuntimeException("Generating visitor handler is only supported for single method interfaces but faild to instantiate ");

		final Method method = methods[0];
		
		Parameter[] parameters = method.getParameters();
		
		List<ResultColumnMeta> columnsList = new ArrayList<ResultColumnMeta>();
		String tableName = "";
		for (int i = 0; i < parameters.length; i++) {
			columnsList.add(makeColumnMeta(clazz, parameters[i], tableName, i));
		}
		
		final ResultColumnMeta[] columns = columnsList.toArray(new ResultColumnMeta[columnsList.size()]); 
		StringBuffer buff = new StringBuffer();

		for(int i=0; i<columns.length; i++){
			if(i>0) buff.append(", ");
			String columnSql = columns[i].getColumnSql();
			String columnTable = columns[i].getTableName();
			if(columnSql != null && !columnSql.isEmpty()){
				buff.append(columnSql).append(" as ");
			}
			if(columnTable != null && !columnTable.isEmpty()){
				buff.append('"').append(columnTable).append('"').append(".");
			}
			buff.append('"').append(columns[i].columnName).append('"');
		}
		
		final String columnNamesStr = buff.toString();
		
		return new IResultVisitor<T>() {

			@Override
			public String getColumnNamesStr() {
				return columnNamesStr;
			}

			@Override
			public void visitResult(ResultSet rs, T fwd) throws SQLException {
				Object[] args = new Object[columns.length];
				for(int i=0; i<columns.length; i++){
					args[i] = columns[i].getGetter().get(rs, i+1); 
				}

				try {
					method.invoke(fwd, args);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new RuntimeException("Problem executing method "+clazz.getName()+"."+method.getName(),e);
				}
			}
		};
	}

	public <T> IResultVisitor<T> loadHandlerFromClass(String cName) {
		try {
			Class<?> meta = Class.forName(cName);
			// found if no exception, now we just need to construct new instance
			if(IResultVisitor.class.isAssignableFrom(meta))
				return (IResultVisitor<T>) meta.getConstructor(new Class<?>[]{ResultGetterSource.class}).newInstance(resultGetterSource);
		} catch (ClassNotFoundException e) {
			// ok, no Meta generated by hipster-processor
		} catch (Exception e) {
			throw new RuntimeException("Found possible generated Visitor handler class, but faild to instantiate ",e);
		}
		return null;
	}

	private ResultColumnMeta makeColumnMeta(Class<?> clazz, Parameter parameter, String tableName, int ordinal) {
		String columnName = parameter.getName();
		String columnSql = "";
		Class<?> returnType = parameter.getType();

		if(HipsterSqlUtil.isPersistenceApiPresent()){
			Column columnAnnotation = parameter.getAnnotation(Column.class);
			if(columnAnnotation != null){
				if(!columnAnnotation.name().isEmpty())  columnName = columnAnnotation.name();
				if(!columnAnnotation.table().isEmpty()) tableName = columnAnnotation.table();
			}			
		}

		HipsterColumn hipsterColumn = parameter.getAnnotation(HipsterColumn.class);
		if(hipsterColumn != null){
			if(!hipsterColumn.name().isEmpty()) columnName = hipsterColumn.name();
			columnSql = hipsterColumn.sql();
			if(!hipsterColumn.table().isEmpty()) tableName = hipsterColumn.table();
		}			
		
		Class<?>[] typeParams = extractGenericArguments(parameter.getParameterizedType());
		IResultGetter<?> getter = resultGetterSource.getForRequired(returnType, typeParams);

		return new ResultColumnMeta(clazz, returnType, parameter.getName(), parameter.getName(), columnName, columnSql, tableName, returnType.isPrimitive(), ordinal, getter, ImmutableList.safe(typeParams)); 
	}

	
	public static Class<?>[] extractGenericArguments(Type returnType){
		if(returnType instanceof ParameterizedType){
			ParameterizedType parameterizedType = (ParameterizedType) returnType;
			Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
			Class<?>[] typeParams = new Class<?>[actualTypeArguments.length];
			for (int i = 0; i < typeParams.length; i++) {
				typeParams[i] = (Class<?>) actualTypeArguments[i];
			}
			
			return typeParams; 
		}else{
			return new Class<?>[0];
		}
	}
	

}
