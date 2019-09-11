package hr.hrg.hipster.dao.test;

import static org.testng.Assert.*;

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
	public void testChangeListenerCall(){
		
		EntityEventHub hub = new EntityEventHub();

		final AtomicInteger callCount = new AtomicInteger();
		
		hub.addChangeListener(new IChangeListener<User1, Long, BaseColumnMeta<?>>() {

			@Override
			public void recordChanged(EntityEvent<User1, Long, BaseColumnMeta<?>> update, long batchId) {
				callCount.incrementAndGet();
			}
		}, User1.class);
		
		User1 old = new User1Immutable(1L, ImmutableList.safe("name"), 22);
		
		User1Update update = new User1Update(old);
		update.age(33);
		User1Meta meta = new User1Meta(new TypeSource(), 0);
		
		User1Immutable newUser = new User1Immutable(update); 

		hub.fireChange(new EntityEvent<User1, Long, BaseColumnMeta>(1L, old, newUser, update, meta), 1l);
		
		assertEquals(callCount.get(), 1);
		
	}

}

