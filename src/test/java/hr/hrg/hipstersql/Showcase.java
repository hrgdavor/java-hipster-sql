package hr.hrg.hipstersql;

import static hr.hrg.hipstersql.QueryUtil.*;

import java.sql.Connection;
import java.sql.PreparedStatement;

// do not run this code :) it will not work :D

public class Showcase {

	public static void main(String[] args) throws Exception{
		Long id = 1L;
		String password = "1";
		String name = "1";
		Query query;
		PreparedStatement pStatement=null;
		Connection conn=null;
		HipsterSql hip = null;
		
		// I prefer to have, the variables that are part of the query, inline with sql code, 
		// but something like this could easily become sql injection problem if name variable comes from user input
		String queryStr = "select * from users where name LIKE'"+name+"%'";

		// in our metohds query parts are passed as arguments (variable number of arguments),  
		// and prepared statments are generated in the background when the query is executed
		// the only difference from the unsafe query string above is that "+" plus operator is replaced by "," comma
		q("select * from users where name LIKE '",name,"%'");

		// Sure, using a simple prepared statement like this is not so bad, but it gets complicated quickly
		pStatement = conn.prepareStatement("select * from users where name LIKE ?");
		pStatement.setString(1, name+"%");

		
		
		//  ******************** examples
		
		// creating new query is not very complicated
		query = new Query("select * from users where id=",id);

		// but shorthand "q" and "query" static methods  
		query = q("select * from users where id=",id);

		// arguably can maybe make code a bit more readable, depending on personal style/preference
		somemethod(new Query("select * from users where id=",id));
		somemethod(query("select * from users where id=",id));
		somemethod(q("select * from users where id=",id));
		
		
		
		
		
		
	}
	

	
	public static void prep(Object ...objects) {
		
	}
	public static void somemethod(Object ...objects) {
		
	}
}
