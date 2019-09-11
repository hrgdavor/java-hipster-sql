package hr.hrg.hipster.dao.test;

import hr.hrg.hipster.dao.*;
import hr.hrg.hipster.sql.*;

public class UserDao extends EntityDao<User, Long, BaseColumnMeta, UserMeta>{

	public UserDao(IHipsterConnection conn) {
		super(User.class, conn);
	}

	
}
