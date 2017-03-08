package hr.hrg.hipstersql;

import static hr.hrg.hipstersql.QueryUtil.*;
import static org.testng.Assert.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.testng.annotations.Test;

@Test
public class TestBuild {

	HipsterSql hip = new HipsterSql(null);

	@Test
	public void testBuild() {

		Long id = 1L;
		String name = "John";
		String gender = "M";
		Integer age = 100;
		String password = "pwd";
		Query query;
		PreparedQuery prepare;
		
		
		Map<Object,Object> data = new LinkedHashMap<>();
		data.put("name", name);
		data.put("gender", gender);
		data.put("age", age);
		// to pass a function call, just pass a new query object instead of the value
		data.put("password", q("PASSWORD(",password,")") );

		// we create a map for this example, but you might have it already by reading data from somewhere
		query = hip.buildInsert("user", data);
		
		prepare = hip.prepare(query);
		assertEquals(prepare.getQueryString(), "INSERT INTO user(name,gender,age,password VALUES(?,?,?,PASSWORD(?))");
		assertEquals(prepare.toString(), "INSERT INTO user(name,gender,age,password VALUES('John','M',100,PASSWORD('pwd'))");
		
		// varargs version if you want to inline the parameters and not create map with data
		query = hip.buildInsertVar("user", 
				"name", name,
				"gender", gender,
				"age", age,
				// to pass a function call, just pass a new query object instead of the value
				"password", q("PASSWORD(",password,")")
			);

		prepare = hip.prepare(query);
		assertEquals(prepare.getQueryString(), "INSERT INTO user(name,gender,age,password VALUES(?,?,?,PASSWORD(?))");
		assertEquals(prepare.toString(), "INSERT INTO user(name,gender,age,password VALUES('John','M',100,PASSWORD('pwd'))");
		
		// buildInsert helps with generating update queries very similar to buildInsert
		// second parameter is filter to limit updates
		
		// we can reuse the map from last example
		query = hip.buildUpdate("user", q("id=",id) ,data);
		
		prepare = hip.prepare(query);
		assertEquals(prepare.getQueryString(), "UPDATE user SET name=?,gender=?,age=?,password=PASSWORD(?) WHERE id=?");
		assertEquals(prepare.toString(), "UPDATE user SET name='John',gender='M',age=100,password=PASSWORD('pwd') WHERE id=1");
		
		// varargs version if you want to inline the parameters and not create map with data
		query = hip.buildUpdateVar("user", hip.buildFilter("id",id),  
				"name", name,
				"gender", gender,
				"age", age,
				// to pass a function call, just pass a new query object instead of the value
				"password", q("PASSWORD(",password,")")
			);

		prepare = hip.prepare(query);
		assertEquals(prepare.getQueryString(), "UPDATE user SET name=?,gender=?,age=?,password=PASSWORD(?) WHERE id = ?");
		assertEquals(prepare.toString(), "UPDATE user SET name='John',gender='M',age=100,password=PASSWORD('pwd') WHERE id = 1");
		
	}
	
}
