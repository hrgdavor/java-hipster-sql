package hr.hrg.hipster.sql;

import java.lang.reflect.*;
import java.util.*;

public class ResultProxyHandler implements InvocationHandler{

	Map<?,?> map;
	
	public ResultProxyHandler(Map<?, ?> map) {
		super();
		this.map = map;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return map.get(method.getName());
	}

}
