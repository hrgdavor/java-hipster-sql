package hr.hrg.hipster.dao.test;

import java.util.*;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.*;

import hr.hrg.hipster.dao.*;
import hr.hrg.hipster.sql.*;

@HipsterEntity(table="user_table")
@HipsterVisitor
public interface User{
	
	@Id
	@Column(name="user_id")
	public Long getId();
	
	@JsonIgnore
	@HipsterColumn(customType=StringListGetter.class)
	public List<String> getName();
	public int getAge();

}
