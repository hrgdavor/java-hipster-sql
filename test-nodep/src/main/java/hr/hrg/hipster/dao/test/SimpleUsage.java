package hr.hrg.hipster.dao.test;

import java.sql.*;
import java.util.*;

import hr.hrg.hipster.sql.*;

public class SimpleUsage {

	public static void main(String[] args) throws Exception{
		
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false");

		// create table and insert few rows
		initData(conn);
		
		// global definitions and data handling (can be Singleton)
        HipsterSql hipSql = new HipsterSql();
        
        // SQL connection specific (not thread safe, use instance per Thread)
        HipsterConnectionImpl hip = new HipsterConnectionImpl(hipSql, conn);

        // "SELECT {column names}" will be added, and column names calculated by analysing the interface
        List<User> users = hip.entities(User.class,"from user_table");		
        for(User user:users){
    		System.out.println(user.getUser_id()+" "+user.getAge()+" "+user.getName());        	
        }

        System.out.println();
        
        String filterText = "%world%";
        users = hip.entities(User.class,"from user_table WHERE name like ", filterText);		
        for(User user:users){
    		System.out.println(user.getUser_id()+" "+user.getAge()+" "+user.getName());        	
        }
	}

	public static void initData(Connection conn) throws Exception{
		
		Statement statement = conn.createStatement();
		statement.execute("CREATE TABLE user_table(user_id INT, name VARCHAR, age int)");
		statement.execute("INSERT INTO user_table VALUES"+
				"(1, 'Hello',11),"+
				"(2, 'small,world',22),"+
				"(3, 'big,world',33),"+
				"(4, 'huge,world',44)");
		
	}
	
	public interface User{
		
		// @Column(name="user_id")
		// we are not using javax.persistence in this example, 
		// so we must name the method properly to get correct column name in SQL
		public Long getUser_id(); 
		
		public String getName();
		
		public int getAge();
	}	
	
}
