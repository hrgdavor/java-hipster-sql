package hr.hrg.hipster.dao.test;

import static hr.hrg.hipster.sql.BooleanEnum.*;

import java.util.*;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.*;

import hr.hrg.hipster.entity.*;
import hr.hrg.hipster.sql.*;

@HipsterEntity(table="user_table", genVisitor = TRUE)
public interface User{
	
	@Id
	@Column(name="user_id")
	public Long getId();
	
	@JsonIgnore
	@HipsterColumn(customType=StringListGetter.class)
	public List<String> getName();
	public int getAge();



}
