package hr.hrg.hipster.dao.test;

import java.sql.*;
import java.util.*;

//import javax.persistence.*;

import hr.hrg.hipster.sql.*;

public class Testh2dbNoDep {
	static {
	    @SuppressWarnings ("unused") Class<?>[] classes = new Class<?>[] {
	    	org.h2.Driver.class
	    };
	}
	
	public static void main(String[] args) throws Exception{
		System.out.println("Starting ");
		long start = System.currentTimeMillis();
		
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false");
		
		Statement statement = conn.createStatement();
		statement.execute("CREATE TABLE user_table(user_id INT, name VARCHAR, age int)");
		statement.execute("INSERT INTO user_table VALUES(1, 'Hello',11), (2, 'small,world',22), (3, 'big,world',33), (4, 'huge,world',44)");
        System.out.println(" created table in "+(System.currentTimeMillis()-start)+"ms");
		

        ResultGetterSource getterSource = new ResultGetterSource();
        getterSource.registerFor(new StringListGetter(), List.class, String.class);

        HipsterSql hipSql = new HipsterSql(new PreparedSetterSource(), new ReaderSource(getterSource));

		HipsterConnectionImpl hip = new HipsterConnectionImpl(hipSql, conn);
        
		System.out.println(" prepared hipster "+(System.currentTimeMillis()-start)+"ms");
		
		System.out.println();
		System.out.println("where name like '%world%'  ...... UserInner interface");
        printUsersInner(hip.entities(UserInner.class,"from user_table WHERE name like ","%world%"));		
		System.out.println(" printed results in "+(System.currentTimeMillis()-start)+"ms");		

		System.out.println();
		System.out.println("where name like '%world%'  ...... UserInner interface");
        printUsersInner(hip.entities(UserInner.class,"from user_table WHERE name like ","%world%"));		
		System.out.println(" printed results in "+(System.currentTimeMillis()-start)+"ms");		
		
		System.out.println();
		System.out.println("where name like '%world%'  ...... column of Long using reader");
        List<Long> longs = hip.column(Long.class,"select user_id from user_table WHERE name like ","%world%");
        for(Long l: longs) System.out.println(l);
		System.out.println(" printed results in "+(System.currentTimeMillis()-start)+"ms");		
		
	}
	
	public static void printUsersInner(List<UserInner> users) {
		for(UserInner user: users){
    		printUser(user);
    	}
	}

	public static void printUser(UserInner user) {
		if(user == null){
			System.out.println("null");
			return;
		}
		System.out.print(user.getUser_id()+" "+user.getAge()+" "+user.getName().size()+" ");
		for(String n:user.getName()) System.out.print(n+", ");
		System.out.println();
	}

	
	public interface UserInner{
		
//		@Id
//		@Column(name="user_id")
//		public Long getId();
		public Long getUser_id(); // we are not using javax.persistence, so we must rename the method to get correct column name in sql
		public List<String> getName();
		public int getAge();

	}	
}
