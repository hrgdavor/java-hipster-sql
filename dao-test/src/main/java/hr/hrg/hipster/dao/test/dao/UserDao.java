package hr.hrg.hipster.dao.test.dao;
 import hr.hrg.hipster.dao.*;
 import hr.hrg.hipster.sql.*;
 import hr.hrg.hipster.dao.test.*;

 public class UserDao extends EntityDao<User, Long, LocalColumnMeta, UserMeta>{

 	public UserDao(IHipsterConnection conn) {

 		super(User.class, conn);
 	}
 }