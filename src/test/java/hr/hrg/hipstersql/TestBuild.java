package hr.hrg.hipstersql;


import static hr.hrg.hipstersql.QueryUtil.*;
import static org.testng.Assert.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.testng.annotations.Test;

@Test
public class TestBuild {

	HipsterSql hip = new HipsterSql();

	@Test
	public void testBuild() {

		Long id = 1L;
		String name = "John";
		String gender = "M";
		Integer age = 100;
		String password = "pwd";
		Query query;
		PreparedQuery prepare;
		
		// LinkedHashMap is used as it keeps the insert order and we can then predict resulting query
		// as iterator over values will use the same order the items were added to the map
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

		
		// buildFilter can be useful as basis for you to allow users to supply filter for queries in a controllable fashion
		
		// with only 2 parameters "=" operator is assumed 
		query = hip.buildFilter("id",id);
		// resulting prepared statement
		// id = ?
		assertEquals(hip.prepare(query).getQueryString(), "id = ?");

		// when handling null, this function becomes even more useful as it changes "id = null" to "id IS NULL"
		query = hip.buildFilter("id",null);
		// resulting prepared statement
		// id IS NULL
		prepare = hip.prepare(query);
		assertEquals(prepare.getQueryString(), "id IS NULL");
		
		// similar behavior is also with "!=" and "<>" operator and null value
		query = hip.buildFilter("id","!=",null);
		assertEquals(hip.prepare(query).getQueryString(), "id IS NOT NULL");
		query = hip.buildFilter("id","<>",null);
		// both resulting in same prepared statement
		// id IS NOT NULL
		assertEquals(hip.prepare(query).getQueryString(), "id IS NOT NULL");

	}
	
}
