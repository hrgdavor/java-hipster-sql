package hr.hrg.hipster.dao.test.entity;

import static hr.hrg.hipster.sql.BooleanEnum.*;

import java.util.*;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.node.*;

import hr.hrg.hipster.dao.test.*;
import hr.hrg.hipster.entity.*;

@HipsterEntity(table="user_table", genVisitor = TRUE, genBuilder = TRUE, genSql = TRUE, genMongo = TRUE, genJson = TRUE)
public interface User{
	
	@Id
	@Column(name="user_id")
	public Long getId();
	
	@JsonIgnore
	@HipsterColumn(customType=StringListGetter.class)
	public List<String> getName();
	public int getAge();

	public boolean hasSomething();

	@HipsterColumn(keepRest = true)
//	public Map getRest();
	public ObjectNode getRest();

}
