package hr.hrg.hipster.dao.test;

import java.sql.*;
import java.util.*;

import hr.hrg.hipster.sql.*;

public class Testh2dbVisitor {
	static {
	    @SuppressWarnings ("unused") Class<?>[] classes = new Class<?>[] {
	    	org.h2.Driver.class
	    };
	}
	
	public static void main(String[] args) throws Exception{
		System.out.println("Starting ");
		long start = System.currentTimeMillis();
		
		Connection conn = Testh2db.makeTestConnection();
		Testh2db.createUserTable(conn);
		
        System.out.println(" created _table in "+(System.currentTimeMillis()-start)+"ms");
		

        HipsterSql hip = new HipsterSql();
        TypeSource typeSource = hip.getTypeSource();
        typeSource.registerFor(new StringListGetter(), List.class, String.class);


//        hipSql.getVisitorSource().registerFor(new UserVisitorSampleHandler(getterSource), UserVisitorSample.class);
        
		HipsterConnectionImpl hc = new HipsterConnectionImpl(hip, conn);
        
		System.out.println(" prepared hipster "+(System.currentTimeMillis()-start)+"ms");

		System.out.println();
		System.out.println("where name like '%world%'  ...... UserInner interface");

		
		Query query = hip.q("select user_id, name, age from user_table WHERE name like ","%world%");

		hc.rowsVisit(query, (UserVisitor) (id, name, age) -> {
			System.out.println("User1 #"+id+" "+name+" "+age);
		});

	}

	
	public interface UserVisitor{

		public void visitUser(
				Long id, 
				String name, 
				int age);
		
	}
	
}
