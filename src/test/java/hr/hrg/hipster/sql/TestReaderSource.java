package hr.hrg.hipster.sql;

import org.testng.annotations.*;
import static org.testng.Assert.*;

import java.util.*;

@Test
public class TestReaderSource {

//	public static void main(String[] args) {
//		new TestReaderSource().testGeneric();
//	}
	
	@Test
	public void testGeneric() {
		Class<?>[] classes = ReaderSource.extractGenericArguments(ITestGeneric.class.getMethods()[0]);
		assertEquals(classes.length, 1);
		assertEquals(classes[0], String.class);
	}

		
	
	@Test
	public void test() {
		ReaderSource readerSource = new ReaderSource(new ResultGetterSource());
		
		IReadMeta<ITestBasicTypes,ResultColumnMeta> reader = readerSource.getOrCreate(ITestBasicTypes.class);
		
		assertNotNull(reader);
		assertEquals(reader.getEntityClass(), ITestBasicTypes.class, "entity class");
		assertEquals(reader.getTableName(), "","table name");
		assertEquals(reader.getColumnCount(), 14, "column count");
		
	}
	
	
	public interface ITestGeneric{
		public List<String> getStringList();
	}
}
