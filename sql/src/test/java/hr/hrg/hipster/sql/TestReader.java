package hr.hrg.hipster.sql;

import static org.testng.Assert.*;

import java.sql.*;

import org.testng.annotations.*;

@Test
public class TestReader {

//	public static void main(String[] args) throws Exception{
//		new TestReader().testRead();
//	}

	public void testRead() throws Exception{
		ReaderSource readerSource = new ReaderSource(new TypeSource());

		
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:");
		
		Statement statement = conn.createStatement();
		statement.execute("CREATE TABLE user_table("+
				"\"int\" INT,"+
				"\"intObj\" INT,"+
				"\"long\" BIGINT,"+
				"\"longObj\" BIGINT,"+
				"\"double\" FLOAT,"+
				"\"doubleObj\" FLOAT,"+
				"\"float\" FLOAT,"+
				"\"floatObj\" FLOAT,"+
				"\"boolean\" BOOLEAN,"+
				"\"booleanObj\" BOOLEAN,"+
				"\"short\" SMALLINT,"+
				"\"shortObj\" SMALLINT,"+
				"\"byte\" TINYINT,"+
				"\"byteObj\" TINYINT"+
				")");
		statement.execute("INSERT INTO user_table VALUES(1, 2, 3, 4, 5.5, 6.6, 7.7, 8.8, true, false, 9, 10, 11, 12)");
		
		IReadMeta<ITestBasicTypes,BaseColumnMeta> reader = readerSource.getOrCreate(ITestBasicTypes.class);
	
		String selectQuery = "select "+reader.getColumnNamesStr()+" FROM user_table ";
		
		ResultSet rs = statement.executeQuery(selectQuery);
		rs.next();
		
		ITestBasicTypes row = reader.fromResultSet(rs);
		
		assertEquals(row.getInt(), 1);
		assertEquals(row.getIntObj().intValue(), 2);
		
		assertEquals(row.getLong(), 3L);
		assertEquals(row.getLongObj().longValue(), 4);

		assertEquals(row.getDouble(), 5.5);
		assertEquals(row.getDoubleObj().doubleValue(), 6.6);

		assertEquals(row.getFloat(), 7.7f);
		assertEquals(row.getFloatObj().floatValue(), 8.8f);

		assertEquals(row.getBoolean(), true);
		assertEquals(row.getBooleanObj().booleanValue(), false);

		assertEquals(row.getShort(), 9);
		assertEquals(row.getShortObj().shortValue(), 10);
		
		assertEquals(row.getByte(), 11);
		assertEquals(row.getByteObj().byteValue(), 12);

	}


}
