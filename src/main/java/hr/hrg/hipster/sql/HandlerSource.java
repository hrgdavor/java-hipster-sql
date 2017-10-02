package hr.hrg.hipster.sql;

import java.util.*;

public class HandlerSource<T> {
	protected HashMap<Class<? extends Object>, T> registered = new HashMap<>(); 
	protected HashMap<Class<? extends Object>, GenericEntry<T>> registeredGeneric = new HashMap<>(); 
    protected HashMap<String, T> named = new HashMap<>();

    public void register(T handler, String name){
    	named.put(name, handler);
    }

    public void registerBoth(T handler, Class<?> clazz, Class<?> ...classParams){
    	register(handler, handler.getClass().getName());
    	registerFor(handler, clazz, classParams);
    }

    public void registerFor(T handler, Class<?> clazz, Class<?> ...classParams){
		if(classParams.length == 0){
			registered.put(clazz, handler);			
		}else{
			GenericEntry<T> genericEntry = registeredGeneric.get(clazz);
			if(genericEntry == null){
				genericEntry = new GenericEntry<T>();
				registeredGeneric.put(clazz, genericEntry);
			}
			genericEntry.add(handler, classParams);
		}
	}

	public T getForRequired(Class<?> clazz, Class<?> ...classParams) {
		T for1 = getFor(clazz, classParams);
		if(for1 == null) throw new RuntimeException("Handler not found for "+clazz.getName()+"<"+HipsterSqlUtil.joinClassNames(",", classParams)+">");
		return for1;
	}
	
	public T getFor(Class<?> clazz, Class<?> ...classParams) {
		if(classParams.length == 0){			
			return (T) registered.get(clazz);
		}else{
			GenericEntry<T> genericEntry = registeredGeneric.get(clazz);
			if(genericEntry != null){
				return genericEntry.get(classParams);
			}	
		}
		return null;
	}

	public T get(String name) {
		return named.get(name);
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
