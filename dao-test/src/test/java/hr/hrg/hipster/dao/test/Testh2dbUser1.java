package hr.hrg.hipster.dao.test;

import java.sql.*;
import java.util.*;

import hr.hrg.hipster.dao.*;
import hr.hrg.hipster.sql.*;
import hr.hrg.hipster.type.*;

public class Testh2dbUser1 {
	static {
	    @SuppressWarnings ("unused") Class<?>[] classes = new Class<?>[] {
	    	org.h2.Driver.class
	    };
	}
	
	public static void main(String[] args) throws Exception{
		System.out.println("Starting "+"1,".split(",").length);
		long start = System.currentTimeMillis();
		
		Connection conn = makeTestConnection();		
		createUser1Table(conn);
		
        System.out.println(" created _table in "+(System.currentTimeMillis()-start)+"ms");
		

        HipsterSql hip = new HipsterSql();
        TypeSource typeSource = hip.getTypeSource();
        typeSource.registerFor(new StringListGetter(), List.class, String.class);
        hip.getEntitySource().register(User1Meta.class);


		HipsterConnectionImpl hc = new HipsterConnectionImpl(hip, conn);

		User1Meta meta = (User1Meta) hip.getEntitySource().getFor(User1.class);
        
		System.out.println(" prepared hipster "+(System.currentTimeMillis()-start)+"ms");

        EntityDao<User1,Long, ColumnMeta, User1Meta> dao = new EntityDao<User1, Long, ColumnMeta, User1Meta>(meta, hc);

		printUser1s(dao.qAll());

		System.out.println(" printed results in "+(System.currentTimeMillis()-start)+"ms");		

		System.out.println();
		System.out.println("By id: 2");
        printUser1(dao.qOneById(2l));
		System.out.println(" printed results in "+(System.currentTimeMillis()-start)+"ms");		

		System.out.println();
		System.out.println("where id>2 order by id desc");
        printUser1s(dao.qAll("WHERE ",meta.id, ">", 2, " order by ", meta.id," desc"));
		System.out.println(" printed results in "+(System.currentTimeMillis()-start)+"ms");		
        				
		System.out.println();
		System.out.println("where name like '%world%'  ...... column of Long using reader");
        List<Long> longs = hc.column(Long.class, "select user_id from user_table WHERE ",meta.name," like ","%world%");
        for(Long l: longs) System.out.println(l);
		System.out.println(" printed results in "+(System.currentTimeMillis()-start)+"ms");		
		
	}

	public static Connection makeTestConnection() throws ClassNotFoundException, SQLException {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false");
		return conn;
	}

	public static void createUser1Table(Connection conn) throws SQLException {
		Statement statement = conn.createStatement();
		statement.execute("CREATE TABLE user_table(user_id INT, name VARCHAR, age int)");
		statement.execute("INSERT INTO user_table VALUES(1, 'Hello',11), (2, 'small,world',22), (3, 'big,world',33), (4, 'huge,world',44)");
	}
	
	public static void printUser1s(List<User1> User1s) {
		for(User1 User1: User1s){
    		printUser1(User1);
    	}
	}

	public static void printUser1(User1 User1) {
		if(User1 == null){
			System.out.println("null");
			return;
		}
		System.out.print(User1.getId()+" "+User1.getAge()+" "+User1.getName().size()+" ");
		for(String n:User1.getName()) System.out.print(n+", ");
		System.out.println();
	}
		
}
