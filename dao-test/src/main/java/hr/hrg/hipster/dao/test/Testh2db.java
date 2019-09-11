package hr.hrg.hipster.dao.test;

import java.sql.*;
import java.util.*;

import javax.persistence.*;

import hr.hrg.hipster.dao.*;
import hr.hrg.hipster.dao.test.Testh2db_.*;
import hr.hrg.hipster.sql.*;

public class Testh2db {
	static {
	    @SuppressWarnings ("unused") Class<?>[] classes = new Class<?>[] {
	    	org.h2.Driver.class
	    };
	}
	
	public static void main(String[] args) throws Exception{
		System.out.println("Starting ");
		long start = System.currentTimeMillis();
		
		Connection conn = makeTestConnection();		
		createUserTable(conn);
		
        System.out.println(" created table in "+(System.currentTimeMillis()-start)+"ms");
		

        HipsterSql hipSql = new HipsterSql();
        TypeSource getterSource = hipSql.getTypeSource();
        
        getterSource.registerFor(new StringListGetter(), List.class, String.class);


		HipsterConnectionImpl hip = new HipsterConnectionImpl(hipSql, conn);

		UserMeta meta = new UserMeta(getterSource, 0);

		System.out.println(" prepared hipster "+(System.currentTimeMillis()-start)+"ms");

        EntityDao<User,Long, BaseColumnMeta, UserMeta> dao = new EntityDao<User, Long, BaseColumnMeta, UserMeta>(meta, hip);

		printUsers(dao.allByCriteria());

		System.out.println(" printed results in "+(System.currentTimeMillis()-start)+"ms");		

		System.out.println();
		System.out.println("By id: 2");
        printUser(dao.byId(2l));
		System.out.println(" printed results in "+(System.currentTimeMillis()-start)+"ms");		

		System.out.println();
		System.out.println("where id>2 order by id desc");
        printUsers(dao.allByCriteria("WHERE ",UserMeta.id, ">", 2, " order by ", UserMeta.id," desc"));
		System.out.println(" printed results in "+(System.currentTimeMillis()-start)+"ms");		
        
		System.out.println();
		System.out.println("where name like '%world%'  ...... UserInnerMeta");
		UserInnerMeta userInnerMeta = new UserInnerMeta(getterSource, 1);
        printUsersInner(hip.entities(userInnerMeta,"from user_table WHERE ",UserInnerMeta.name," like ","%world%"));		
		System.out.println(" printed results in "+(System.currentTimeMillis()-start)+"ms");		
		
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
        List<Long> longs = hip.column(Long.class,"select user_id from user_table WHERE ",UserMeta.name," like ","%world%");
        for(Long l: longs) System.out.println(l);
		System.out.println(" printed results in "+(System.currentTimeMillis()-start)+"ms");		

		
		System.out.println();
		String[] entityNamesPrefix = HipsterSqlUtil.entityNamesPrefixArray(UserInner.class);	
		System.out.println(entityNamesPrefix[0]);
		System.out.println(entityNamesPrefix[1]);
	}

	public static Connection makeTestConnection() throws ClassNotFoundException, SQLException {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false");
		return conn;
	}

	public static void createUserTable(Connection conn) throws SQLException {
		Statement statement = conn.createStatement();
		statement.execute("CREATE TABLE user_table(user_id INT, name VARCHAR, age int)");
		statement.execute("INSERT INTO user_table VALUES(1, 'Hello',11), (2, 'small,world',22), (3, 'big,world',33), (4, 'huge,world',44)");
	}
	
	public static void printUsers(List<User> users) {
		for(User user: users){
    		printUser(user);
    	}
	}

	public static void printUser(User user) {
		if(user == null){
			System.out.println("null");
			return;
		}
		System.out.print(user.getId()+" "+user.getAge()+" "+user.getName().size()+" ");
		for(String n:user.getName()) System.out.print(n+", ");
		System.out.println();
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
		System.out.print(user.getId()+" "+user.getAge()+" "+user.getName().size()+" ");
		for(String n:user.getName()) System.out.print(n+", ");
		System.out.println();
	}

	
	@HipsterEntity(genUpdate=false)
	public interface UserInner{
		
		@Id
		@Column(name="user_id")
		public Long getId();
		public List<String> getName();
		public int getAge();

	}	
}
