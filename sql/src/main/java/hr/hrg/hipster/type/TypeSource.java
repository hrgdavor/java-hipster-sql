package hr.hrg.hipster.type;

import java.util.*;

import org.joda.time.*;

import hr.hrg.hipster.sql.*;

@SuppressWarnings("rawtypes")
public class TypeSource{
	protected HashMap<Class<? extends Object>, ICustomType<?>> registered = new HashMap<>(); 
	protected HashMap<Class<? extends Object>, GenericEntry<ICustomType<?>>> registeredGeneric = new HashMap<>(); 

	protected HashMap<Class<?>, ICustomType> instances = new HashMap<>();
    protected HashMap<String, ICustomType> named = new HashMap<>();

    public TypeSource() {
    	registerInstance(new BooleanIntType());

    	registerAll(new BooleanType(), Boolean.class);
		registerAll(new IntegerType(), Integer.class);
		registerAll(new LongType(), Long.class);
		registerAll(new FloatType(), Float.class);
		registerAll(new DoubleType(), Double.class);
		registerAll(new ShortType(), Short.class);
		registerAll(new ByteType(), Byte.class);

		registerAll(new BooleanType(), boolean.class);
		registerAll(new IntegerType(), int.class);
		registerAll(new LongType(), long.class);
		registerAll(new FloatType(), float.class);
		registerAll(new DoubleType(), double.class);
		registerAll(new ShortType(), short.class);
		registerAll(new ByteType(), byte.class);
		
		registerAll(new StringType(), String.class);

		registerAll(new DateType(), java.util.Date.class);
		registerAll(new SqlDateType(), java.sql.Date.class);
		registerAll(new SqlTimeType(), java.sql.Time.class);
		registerAll(new SqlTimestampType(), java.sql.Timestamp.class);
		
		if(HipsterSqlUtil.isYodaPresent()) {
			registerAll(new DateTimeType(), DateTime.class);
			registerAll(new LocalTimeType(), LocalTime.class);
			registerAll(new LocalDateType(), LocalDate.class);
		}
	}

    public void registerNamed(ICustomType handler, String name){
    	named.put(name, handler);
    }

    public void registerInstance(ICustomType handler){
    	instances.put(handler.getClass(), handler);
    }

    public void registerInstance(ICustomType handler, Class<?> clazz){
    	instances.put(clazz, handler);
    }

    /** Register as handler for specific class*/
    public void registerAll(ICustomType handler, Class<?> clazz){
    	registerNamed(handler, handler.getClass().getName());
    	registerFor(handler, clazz);
    }

    public void registerFor(ICustomType handler, Class<?> clazz, Class<?> ...classParams){
    	registerInstance(handler, handler.getClass());
		if(classParams.length == 0){
			registered.put(clazz, handler);			
		}else{
			GenericEntry<ICustomType<?>> genericEntry = registeredGeneric.get(clazz);
			if(genericEntry == null){
				genericEntry = new GenericEntry<ICustomType<?>>();
				registeredGeneric.put(clazz, genericEntry);
			}
			genericEntry.add(handler, classParams);
		}
	}

	@SuppressWarnings("unchecked")
	public ICustomType getForRequired(Class<?> clazz, Class<?> ...classParams) {
		ICustomType for1 = getFor(clazz, classParams);
		if(for1 == null && clazz.isEnum()) {
			for1 = new EnumType(clazz);
		}
		if(for1 == null) {
			throw new RuntimeException("Handler not found for "+clazz.getName()+"<"+HipsterSqlUtil.joinClassNames(",", classParams)+">");
		}
		return for1;
	}
	
	public ICustomType getFor(Class<?> clazz, Class<?> ...classParams) {
		if(classParams.length == 0){			
			return (ICustomType) registered.get(clazz);
		}else{
			GenericEntry<ICustomType<?>> genericEntry = registeredGeneric.get(clazz);
			if(genericEntry != null){
				return genericEntry.get(classParams);
			}	
		}
		return null;
	}

	public ICustomType getInstance(Class<?> clazz) {
		return instances.get(clazz);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends ICustomType> T getInstanceRequired(Class<T> clazz) {
		T handler = (T) instances.get(clazz);
		if(handler == null) throw new RuntimeException("Instance not found "+clazz);
		return handler;
	}
	
	public ICustomType getNamed(String name) {
		return named.get(name);
	}
	
	public ICustomType getNamedRequired(String name){
		ICustomType handler = named.get(name);
		if(handler == null) throw new RuntimeException("Handler not found "+name);
		return handler;
	}

	static class GenericEntry<T1>{
		List<T1> handlers = new ArrayList<>();
		List<Class<?>[]> handlerParams = new ArrayList<>();
		
		public T1 get(Class<?> ...params){
			for(int i=0; i<this.handlerParams.size(); i++){
				Class<?>[] tmp = handlerParams.get(i);
				if(tmp.length != params.length) continue;
				boolean eq = true;
				for(int p=0; p<params.length; p++){
					if(!params[p].equals(tmp[p])){
						eq = false;
						break;
					}
				}
				if(eq) return handlers.get(i);
			}
			return null;
		}

		public void add(T1 getter, Class<?> ...params){
			handlers.add(getter);
			handlerParams.add(params);
		}
	}
	
}
