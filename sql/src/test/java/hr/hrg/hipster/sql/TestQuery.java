package hr.hrg.hipster.sql;

import static org.testng.Assert.*;

import org.testng.annotations.*;


@Test
public class TestQuery {
	static 	HipsterSql hip = new HipsterSql();
		

	static Query q1 = hip.q(" and user_id=",1);
	static Query q2 = hip.q(" and is_deleted=",0);
	
	
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
						hip.q("select * from users WHERE 1=1",q1," ORDER BY name")),

				// create query with first part and append rest
				qp("select * from users WHERE 1=1 and user_id=1 ORDER BY name",
						"select * from users WHERE 1=1 and user_id=? ORDER BY name",
						hip.q("select * from users WHERE 1=1").addParts(q1," ORDER BY name")),
				
				// create query with first 2 parts and append rest
				qp("select * from users WHERE 1=1 and user_id=1 ORDER BY name",
						"select * from users WHERE 1=1 and user_id=? ORDER BY name",
						hip.q("select * from users WHERE 1=1",q1).addParts(" ORDER BY name")),

				qp("select * from users WHERE 1=1 and user_id=1 and is_deleted=0 ORDER BY name",
						"select * from users WHERE 1=1 and user_id=? and is_deleted=? ORDER BY name",
						"select * from users WHERE 1=1",q1,q2," ORDER BY name"),
				
				// create empty query and append
				qp("select * from users WHERE 1=1 and user_id=1 and is_deleted=0 ORDER BY name",
						"select * from users WHERE 1=1 and user_id=? and is_deleted=? ORDER BY name",
						hip.q("select * from users WHERE 1=1",q1,q2," ORDER BY name")),
				
				// create query with first part and append rest
				qp("select * from users WHERE 1=1 and user_id=1 and is_deleted=0 ORDER BY name",
						"select * from users WHERE 1=1 and user_id=? and is_deleted=? ORDER BY name",
						hip.q("select * from users WHERE 1=1").addParts(q1,q2," ORDER BY name")),

		};
	}
	
	@Test(dataProvider="queries")
	public void testQuery(String queryString, String preparedStr, Object[] p){
		Query query = hip.q(p);
		assertEquals(query.toString(), queryString);
	}

	@Test(dataProvider="queries")
	public void testPrepared(String queryString, String preparedStr, Object[]p){
		Query query = hip.q(p);
		assertEquals(query.getQueryExpressionBuilder().toString(), preparedStr);
		assertEquals(query.toString(), queryString);
	}

	private static Object[] qp(String queryString, String prep, Object ... p) {
		return new Object[]{queryString, prep, p};
	}

}
