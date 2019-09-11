package hr.hrg.hipster.dao.test;

import java.util.*;

import javax.persistence.*;

import hr.hrg.hipster.dao.*;
import hr.hrg.hipster.sql.*;

@HipsterEntity(table="user_table")
@HipsterVisitor
public interface User1{

	@Id
	@Column(name="user_id")
	public Long getId();
	public List<String> getName();
	public int getAge();

}
