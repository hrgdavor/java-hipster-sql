package hr.hrg.hipster.dao.test;

import java.util.*;

import hr.hrg.hipster.sql.*;

@HipsterVisitor
public interface UserVisitorSample {

	public void visitUser(
			@HipsterColumn(name="user_id")
			Long id, 
			List<String> name, 
			int age);
	
}
