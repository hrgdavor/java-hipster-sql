package hr.hrg.hipster.dao.test.dao;

import hr.hrg.hipster.dao.test.*;
import hr.hrg.hipster.dao.test.entity.*;
import hr.hrg.hipster.entity.*;
import hr.hrg.hipster.sql.*;
 
public class UserDao extends EntityDao<User, Long, LocalColumnMeta, UserMeta>{

 	public UserDao(IHipsterConnection conn) {

 		super(User.class, conn);
 	}
 }