package hr.hrg.hipster.dao.test.entity;

import java.util.*;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.*;

import hr.hrg.hipster.dao.test.*;
import hr.hrg.hipster.entity.*;

@HipsterEntity(table="user_big_table")
public interface UserBig{
	
	@Id
	@Column(name="user_id")
	public Long getId();
	
	@JsonIgnore
	@HipsterColumn(customType=StringListGetter.class)
	public List<String> getName();
	public int getAge();

	public String getName4();
	public String getName5();
	public String getName6();
	public String getName7();
	public String getName8();
	public String getName9();
	public String getName10();
	public String getName11();
	public String getName12();
	public String getName13();
	public String getName14();
	public String getName15();
	public String getName16();
	public String getName17();
	public String getName18();
	public String getName19();
	public String getName20();
	public String getName21();
	public String getName22();
	public String getName23();
	public String getName24();
	public String getName25();
	public String getName26();
	public String getName27();
	public String getName28();
	public String getName29();
	public String getName30();
	public String getName31();
	public String getName32();
	public String getName33();
	public String getName34();
	public String getName35();
	public String getName36();
	public String getName37();
	public String getName38();
	public String getName39();
	public String getName40();
	public String getName41();
	public String getName42();
	public String getName43();
	public String getName44();
	public String getName45();
	public String getName46();
	public String getName47();
	public String getName48();
	public String getName49();
	public String getName50();
	public String getName51();
	public String getName52();
	public String getName53();
	public String getName54();
	public String getName55();
	public String getName56();
	public String getName57();
	public String getName58();
	public String getName59();
	public String getName60();
	public String getName61();
	public String getName62();
	public String getName63();
	public String getName64();
	public String getName65();
	public String getName66();
	public String getName67();
	public String getName68();
	public String getName69();

}
