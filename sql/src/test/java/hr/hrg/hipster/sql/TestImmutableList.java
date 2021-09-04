package hr.hrg.hipster.sql;

import java.util.*;

import org.testng.annotations.*;

@Test
public class TestImmutableList {
	
	public static void main(String[] args) {
		new TestImmutableList().testMapFromList();
	}
	
	@SuppressWarnings("rawtypes")
	public void testMapFromList(){
		ImmutableList<Integer> list = new ImmutableList<>(comparator,Arrays.asList(5,8,1,2));
		System.err.println(list.join(","));
		
		list = list.replaceMakeNew(comparator,5, 9);
		System.err.println(list.join(","));		
	}
		
	Comparator<Integer> comparator = new Comparator<Integer>() {
		
		@Override
		public int compare(Integer o1, Integer o2) {
			return o2.intValue() - o1.intValue();
		}
	};
}
