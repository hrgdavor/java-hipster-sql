package hr.hrg.hipster.dao.test;

import static org.testng.Assert.*;

import java.util.*;
import java.util.concurrent.atomic.*;

import org.testng.annotations.*;

import hr.hrg.hipster.dao.change.*;
import hr.hrg.hipster.sql.*;

@Test
public class TestChange {

	@Test(enabled=false)
	public static void main(String[] args) {
		new TestChange().testChangeListenerCall();
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testChangeListenerCall(){
		HipsterSql hip = new  HipsterSql();
		hip.getTypeSource().registerFor(new StringListGetter(), List.class, String.class);
		hip.getEntitySource().register(User1Meta.class);
		
		User1Meta userMeta = (User1Meta) hip.getEntitySource().getFor(User1.class);
		
		final AtomicInteger callCount = new AtomicInteger();
		
		hip.getEventHub().addListener(User1.class,(IEntityEventListener<User1, Long, User1Meta>)
				(type, id, old, updated, delta, meta, batchId) -> {
				System.out.println("delta:"+delta);
				for(int i=0; i<meta.getColumnCount(); i++) {
					if(delta.isChanged(i)) {
						System.out.println("changed: "+meta.getColumn(i)+":\t "+delta.getValue(i));
					}
				}
				callCount.incrementAndGet();
			
		}, EntityEventType.AFTER_CHANGE);
	
		User1 old = new User1Immutable(1L, ImmutableList.safe("name"), 22);
		
		User1Update update = new User1Update(old);
		update.age(33);
		
		LocalColumnMeta ageColumn = userMeta.getColumn("age");
		System.out.println(update.getValue(ageColumn.ordinal()));
		Integer age = update.getValue(userMeta.age);
		System.out.println(age);
		
		User1Immutable newUser = new User1Immutable(update); 

		hip.getEventHub().fireEvent(EntityEventType.AFTER_CHANGE,1L, old, newUser, update, userMeta, 1l);
		
		assertEquals(callCount.get(), 1);
		
	}

}

