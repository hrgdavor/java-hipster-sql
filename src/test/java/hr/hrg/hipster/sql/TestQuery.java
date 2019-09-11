package hr.hrg.hipster.sql;

import static hr.hrg.hipster.sql.QueryUtil.*;
import static org.testng.Assert.*;

import org.testng.annotations.*;
import static hr.hrg.hipster.sql.QueryUtil.*;


@Test
public class TestQuery {
	HipsterSql hip = new HipsterSql();

	static Query q1 = q(" and user_id=",1);
	static Query q2 = q(" and is_deleted=",0);
	
	@DataProvider
	public Object[][] queries(){
		return new Object[][]{
				qp("select id,name from users where id=1",
					"select id,name from users where id=?",
					"select id,name from users where id=",1),

				qp("select id,'name' from users where id=1",
					"select id,? from users where id=?",
					"select id,","name"," from users where id=",1),

				qp("select id,'name' from users where birthday='1990\\\\03\\\\05'",
						"select id,? from users where birthday=?",
						"select id,","name"," from users where birthday=","1990\\03\\05"),

				qp("select * from users WHERE 1=1 and user_id=1 ORDER BY name",
						"select * from users WHERE 1=1 and user_id=? ORDER BY name",
						"select * from users WHERE 1=1",q1," ORDER BY name"),

				// create empty query and append
				qp("select * from users WHERE 1=1 and user_id=1 ORDER BY name",
						"select * from users WHERE 1=1 and user_id=? ORDER BY name",
						q().append("select * from users WHERE 1=1",q1," ORDER BY name")),

				// create query with first part and append rest
				qp("select * from users WHERE 1=1 and user_id=1 ORDER BY name",
						"select * from users WHERE 1=1 and user_id=? ORDER BY name",
						q("select * from users WHERE 1=1").append(q1," ORDER BY name")),
				
				// create query with first 2 parts and append rest
				qp("select * from users WHERE 1=1 and user_id=1 ORDER BY name",
						"select * from users WHERE 1=1 and user_id=? ORDER BY name",
						q("select * from users WHERE 1=1",q1).append(" ORDER BY name")),

				qp("select * from users WHERE 1=1 and user_id=1 and is_deleted=0 ORDER BY name",
						"select * from users WHERE 1=1 and user_id=? and is_deleted=? ORDER BY name",
						"select * from users WHERE 1=1",q1,q2," ORDER BY name"),
				
				// create empty query and append
				qp("select * from users WHERE 1=1 and user_id=1 and is_deleted=0 ORDER BY name",
						"select * from users WHERE 1=1 and user_id=? and is_deleted=? ORDER BY name",
						q().append("select * from users WHERE 1=1",q1,q2," ORDER BY name")),
				
				// create query with first part and append rest
				qp("select * from users WHERE 1=1 and user_id=1 and is_deleted=0 ORDER BY name",
						"select * from users WHERE 1=1 and user_id=? and is_deleted=? ORDER BY name",
						q("select * from users WHERE 1=1").append(q1,q2," ORDER BY name")),

		};
	}
	
	@Test(dataProvider="queries")
	public void testQuery(String queryString, String preparedStr, Object[] p){
		assertEquals(q(p).toString(), queryString);
	}

	@Test(dataProvider="queries")
	public void testPrepared(String queryString, String preparedStr, Object[]p){
		PreparedQuery prepare = hip.prepare(p);
		assertEquals(prepare.getQueryString(), preparedStr);
		assertEquals(prepare.toString(), queryString);
	}

	@DataProvider
	public Object[][] implodeData(){
		return new Object[][]{
			toArray("id=1",
					join( toList(q("id=",1)) , " AND ")),

			toArray("id=1 AND date>NOW()",
					join( toList(q("id=",1),q("date>",q("NOW()"))) , " AND ")),

			toArray("id=1 AND date>'2016-01-01'",
					join( toList(q("id=",1),q("date>","2016-01-01")) , " AND ")),

			toArray("WHERE id=1 AND date>'2016-01-01'",
					join( "WHERE ", toList(q("id=",1),q("date>","2016-01-01")) , " AND ")),

			toArray("",
					join( toList(q()) , " AND ")),

			toArray("",
					join( "WHERE ", toList(q()) , " AND ")),
		};
	}

	@Test(dataProvider="implodeData")
	public void testImplode(String queryString, Query query){
		assertEquals(query.toString(), queryString);
		
	}

	@DataProvider
	public Object[][] implodeData2(){
		return new Object[][]{
			toArray("1,2",
					joinValues( toList(1,2) , ",")),
			
			toArray(" IN(1,2,3) ",
					joinValues(" IN(", toList(1,2,3) , ",", ") ")),
			
			toArray(" IN(1,2,3,4) ",
					queryIn(1,2,3,4)),

		};
	}

	@Test(dataProvider="implodeData2")
	public void testImplodeValues(String queryString, Query query){
		assertEquals(query.toString(), queryString);
		
	}

	private static Object[] qp(String queryString, String prep, Object ... p) {
		return new Object[]{queryString, prep, p};
	}

	private static Query q(Object ... parts) {
		return new Query(parts);
	}

}
