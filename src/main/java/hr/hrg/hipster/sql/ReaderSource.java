package hr.hrg.hipster.sql;

import java.lang.reflect.*;

import java.util.*;
import java.util.concurrent.*;

import javax.persistence.*;

public class ReaderSource {

	protected Map<Class<? extends Object>, IReadMeta<?, ? extends BaseColumnMeta>> registered = new ConcurrentHashMap<>(); 

	private TypeSource typeSource;

	private int ordinalIndex;
	
	public ReaderSource(TypeSource typeSource){
		this.typeSource = typeSource;
	}

	public TypeSource getTypeSource() {
		return typeSource;
	}

	public <T> void registerFor(IReadMeta<T, ? extends BaseColumnMeta> handler, Class<T> clazz){
		registered.put(clazz, handler);
	}

	public <T> void registerFor(IReadMeta<T, ? extends BaseColumnMeta> handler){
		registered.put(handler.getEntityClass(), handler);
	}

	/** 
	 * @param clazz Class that is registered/generated by {@link IReadMeta}
	 * @return {@link IReadMeta} or throw RuntimeException if none registered
	 */
	public <T> IReadMeta<T, ? extends BaseColumnMeta> getForRequired(Class<T> clazz) {
		IReadMeta<T, ? extends BaseColumnMeta> for1 = getFor(clazz);
		if(for1 == null) throw new RuntimeException("Handler not found for "+clazz.getName());
		return for1;
	}

	/** 
	 * @param clazz Class that is generated by {@link IReadMeta}
	 * @return {@link IReadMeta} or null if none registered
	 */
	@SuppressWarnings("unchecked")
	public <T> IReadMeta<T, ? extends BaseColumnMeta> getFor(Class<T> clazz) {
		return (IReadMeta<T, ? extends BaseColumnMeta>) registered.get(clazz);
	}
	
	/** Create a read-only proxy version if no {@link IReadMeta} is registered yet
	 * 
	 * @param clazz Class that is generated by {@link IReadMeta}
	 * @return {@link IReadMeta}
	 */
	@SuppressWarnings("unchecked")
	public <T, E extends BaseColumnMeta> IReadMeta<T, E> getOrCreate(Class<T> clazz) {
		IReadMeta<T, E> ret = (IReadMeta<T, E>) registered.get(clazz);
		if(ret == null){
			// do not care about concurrency here, if two threads cause creation the resulting ReaderSource will be the same
			// so it is not important which version ends-up in the map
			if(!clazz.isInterface()) throw new RuntimeException("Only interfaces are supported for generating IResultGetter Proxy ");
			
			ret = newResultGetter(clazz);
			registered.put(clazz, (IReadMeta<?, BaseColumnMeta>) ret);
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	<T, E extends BaseColumnMeta> IReadMeta<T, E> newResultGetter(Class<T> clazz) {

		// use same naming convention as hipster-processor and try to load that class
		String cName = HipsterSqlUtil.entityNamesPrefix(clazz)+"Meta";
		try {
			Class<?> meta = Class.forName(cName);
			// found if no exception, now we just need to construct new instance
			return (IReadMeta<T, E>) meta.getConstructor(new Class<?>[]{TypeSource.class, int.class}).newInstance(typeSource, ordinalIndex++);
		} catch (ClassNotFoundException e) {
			// ok, no Meta generated by hipster-processor
		} catch (Exception e) {
			throw new RuntimeException("Found possible generated Meta class "+cName+", but faild to instantiate ",e);
		}
		
		// no Meta generated by hipster-processor, create one that returns results via Proxy
		Method[] methods = clazz.getMethods();
		List<BaseColumnMeta> columns = new ArrayList<>();
		List<ICustomType<?>> getters = new ArrayList<>();
		String tableName = "";
		for (int i = 0; i < methods.length; i++) {
			String methodName = methods[i].getName();
			int idx = -1;
			if(methodName.startsWith("get")){
				idx = 3;
			}else if(methodName.startsWith("is")){
				idx = 2;
			}
			if(idx != -1 && idx <methodName.length() && Character.isUpperCase(methodName.charAt(idx))){
				String name = Character.toLowerCase(methodName.charAt(idx)) + methodName.substring(idx+1);
				Class<?> returnType = methods[i].getReturnType();
				Class<?>[] typeParams = extractGenericArguments(methods[i]);
				
				BaseColumnMeta columnMeta = makeColumnMeta(clazz, methods[i], methodName, name, tableName, i, returnType, typeParams);
				columns.add(columnMeta);

				ICustomType<?> getter = typeSource.getForRequired(returnType, typeParams);
				getters.add(getter);
			}
		}
		
		return (IReadMeta<T, E>) new ReaderMeta<>(clazz, tableName, columns, getters);
	}

	private BaseColumnMeta<?> makeColumnMeta(Class<?> clazz, Method method, String methodName, String name, String tableName, int ordinal, Class<?> returnType, Class<?> ...typeParams) {
		String columnName = name;
		String columnSql = "";
		
		Column columnAnnotation = null;

		if(HipsterSqlUtil.isPersistenceApiPresent()){
			columnAnnotation = method.getAnnotation(Column.class);
			if(columnAnnotation != null){
				if(!columnAnnotation.name().isEmpty())  columnName = columnAnnotation.name();
				if(!columnAnnotation.table().isEmpty()) tableName = columnAnnotation.table();
			}			
		}

		HipsterColumn hipsterColumn = method.getAnnotation(HipsterColumn.class);
		if(hipsterColumn != null){
			if(!hipsterColumn.name().isEmpty()) columnName = hipsterColumn.name();
			columnSql = hipsterColumn.sql();
			if(!hipsterColumn.table().isEmpty()) tableName = hipsterColumn.table();
		}			
		
		Class<?> returnTypePrimitive = returnType;
		returnType = HipsterSqlUtil.wrap(returnType);

		return new BaseColumnMeta(ordinalIndex++, name, columnName, methodName,clazz, returnType, returnTypePrimitive, tableName, columnSql, typeParams); 
	}
	
	public static Class<?>[] extractGenericArguments(Method method){
		Type returnType = method.getGenericReturnType();
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
