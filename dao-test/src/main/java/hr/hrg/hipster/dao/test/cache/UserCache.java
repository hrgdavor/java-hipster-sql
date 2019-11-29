package hr.hrg.hipster.dao.test.cache;
 import hr.hrg.hipster.dao.test.entity.*;
import hr.hrg.hipster.entity.*;
import hr.hrg.hipster.sql.*;

 public class UserCache extends SimpleEntityCache<User, Long, UserMeta>{

 	public UserCache(HipsterSql hipster) {

 		super(hipster.getEventHub(), (UserMeta)hipster.getEntitySource().getFor(User.class));
 	}
 }