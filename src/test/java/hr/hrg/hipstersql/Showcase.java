package hr.hrg.hipstersql;

import static hr.hrg.hipstersql.QueryUtil.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

// do not run this code :) it will not work :D

public class Showcase {

	public static void main(String[] args) throws Exception{
		Long id = 1L;
		String password = "pwd";
		String name = "John";
		String gender = "M";
		Integer age = 100;
		Query query;
		PreparedStatement pStatement=null;
		Connection conn=null;
		HipsterSql hip = null;
		
		// I prefer to have, the variables that are part of the query, inline with sql code, 
		// but something like this could easily become sql injection problem if name variable comes from user input
		String queryStr = "select * from users where name LIKE'"+name+"%'";

		// in our metohds query parts are passed as arguments and prepared statments are used in the background 
		// the only difference from the unsafe query string above is that "+" plus operator is replaced by "," comma
		q("select * from users where name LIKE '",name,"%'");

		// Sure, using a simple prepared statement like this is not so bad, but it gets complicated quickly
		pStatement = conn.prepareStatement("select * from users where name LIKE ?");
		pStatement.setString(1, name+"%");

		
		
		//  ******************** QueryUtil
		
		// creating new query is not very complicated
		query =    new Query("select * from users where id=",id);
		somemethod(new Query("select * from users where id=",id));

		// arguably can maybe make code a bit more readable, by using QueryUtil.q static method 
		// (depends on personal style/preference)
		query =    q("select * from users where id=",id);
		somemethod(q("select * from users where id=",id));

		
		//  ******************** HipsterSql
		
		// we create a map for this example, but you might have it already by reading data from somewhere
		Map<Object,Object> data = new HashMap<>();
		data.put("name", name);
		data.put("gender", gender);
		data.put("age", gender);
		// to pass a function call, just pass a new query object instead of the value
		data.put("password", q("PASSWORD(",password,")") );
		
		
		// buildInsert helps with generating insert queries

		hip.buildInsert("user", data);
		
		// varargs version if you want to inline the parameters and not create map with data
		hip.buildInsertVar("user", 
				"name", name,
				"gender", gender,
				"age", gender,
				// to pass a function call, just pass a new query object instead of the value
				"password", q("PASSWORD(",password,")")
			);
		// resulting prepared statement 
		// INSERT INTO user(name,gender,age,password VALUES(?,?,?,PASSWORD(?))

		
		
		// buildUpdate helps with generating update queries very similar to buildInsert.
		// The second parameter is filter to limit updates scope
		
		// we can reuse the map from last example
		hip.buildUpdate("user", q("id=",id) ,data);
		
		// varargs version if you want to inline the parameters and not create map with data
		hip.buildUpdateVar("user", q("id=",id),  
				"name", name,
				"gender", gender,
				"age", gender,
				// to pass a function call, just pass a new query object instead of the value
				"password", q("PASSWORD(",password,")")
			);
		// resulting prepared statement 
		// UPDATE user SET name=?,gender=?,age=?,password=PASSWORD(?) WHERE id = ?
		
	}
	

	
	public static void prep(Object ...objects) {
		
	}
	public static void somemethod(Object ...objects) {
		
	}
}
