package hr.hrg.hipstersql;

import static hr.hrg.hipstersql.QueryUtil.*;
import static org.testng.Assert.*;

import java.util.*;

import org.testng.annotations.*;

@Test
public class TestMap {
	
	
	@SuppressWarnings("rawtypes")
	public void testMapFromList(){
		Map<Object, Object> map = new HashMap<>();
		QueryUtil.addRowToTree(map, toObjectList(1,2,"a"));
		QueryUtil.addRowToTree(map, toObjectList(1,3,"b"));
		QueryUtil.addRowToTree(map, toObjectList(2,4,"c"));
		
		assertTrue(map.get(1) instanceof Map);
		Map map1 = (Map) map.get(1);
		assertEquals(map1.get(2),"a");
		assertEquals(map1.get(3),"b");	
		
		Map map2 = (Map) map.get(2);
		assertEquals(map2.get(4),"c");	
	}

	@SuppressWarnings("rawtypes")
	public void testMapFromAssoc(){
		String[] columns = toArray("c1","c2","c3");

		Map<Object, Object> map = new HashMap<>();
		QueryUtil.addRowToTree(map, toMap("c1",1, "c2",2, "c3","a"), columns);
		QueryUtil.addRowToTree(map, toMap("c1",1, "c2",3, "c3","b"), columns);
		QueryUtil.addRowToTree(map, toMap("c1",2, "c2",4, "c3","c"), columns);
		
		assertTrue(map.get(1) instanceof Map);
		Map map1 = (Map) map.get(1);
		assertEquals(map1.get(2),"a");
		assertEquals(map1.get(3),"b");	

		Map map2 = (Map) map.get(2);
		assertEquals(map2.get(4),"c");	
	}
	
}
