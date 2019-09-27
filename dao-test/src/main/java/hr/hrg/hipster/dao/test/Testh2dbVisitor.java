package hr.hrg.hipster.dao.test;

import java.sql.*;
import java.util.*;

import hr.hrg.hipster.dao.*;
import hr.hrg.hipster.query.*;
import hr.hrg.hipster.sql.*;
import hr.hrg.hipster.type.*;

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
        typeSource.registerInstance(new StringListGetter());

        hip.getEntitySource().register(hr.hrg.hipster.dao.test.AllEntitiesInPackage.ALL_META);


		HipsterConnectionImpl hc = new HipsterConnectionImpl(hip, conn);
        
		System.out.println(" prepared hipster "+(System.currentTimeMillis()-start)+"ms");

		System.out.println();
		System.out.println("where name like '%world%'  ...... UserInner interface");

		
		UserMeta meta = (UserMeta) hip.getEntitySource().getFor(User.class);
		Query query = hip.qSelect(meta)
				.add(" WHERE ",meta.name)
				.add(" like ","%world%");
		
		System.out.println(query);
		
		meta.visitResults(hc, query, (id, name, age) -> {
			System.out.println("User Visitor #"+id+" "+name+" "+age);
		});
		
//		hc.rowsVisit(query, (UserVisitor) (id, name, age) -> {
//			System.out.println("User1 #"+id+" "+name+" "+age);
//		});

	}

	
	public interface UserVisitor{

		public void visitUser(
				Long id, 
				String name, 
				int age);
		
	}
	
}
