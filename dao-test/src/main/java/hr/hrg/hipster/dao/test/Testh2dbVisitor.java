package hr.hrg.hipster.dao.test;

import java.sql.*;
import java.util.*;

import hr.hrg.hipster.query.*;
import hr.hrg.hipster.sql.*;

public class Testh2dbVisitor {
	
	public static void main(String[] args) throws Exception{
				
		Connection conn = Testh2db.makeTestConnection();
		Testh2db.createUserTable(conn);
		
        HipsterSql hip = new HipsterSql();
        hip.getTypeSource().registerFor(new StringListGetter(), List.class, String.class);

        hip.getEntitySource().register(hr.hrg.hipster.dao.test.AllEntitiesInPackage.ALL_META);
        UserMeta meta = (UserMeta) hip.getEntitySource().getFor(User.class);


		HipsterConnectionImpl hc = new HipsterConnectionImpl(hip, conn);
        		
		Query query = hip.qSelect(meta)
				.add(" WHERE ",meta.name)
				.add(" like ","%world%");
		
		meta.visitResults(hc, query, (id, name, age) -> {
			System.out.println("User Visitor #"+id+" "+name+" "+age);
		});

		
		System.out.println(query);

	}
	
}
