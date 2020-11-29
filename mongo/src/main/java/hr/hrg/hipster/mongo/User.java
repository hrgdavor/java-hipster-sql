package hr.hrg.hipster.mongo;

import static hr.hrg.hipster.sql.BooleanEnum.*;

import java.util.*;

import javax.persistence.*;

import hr.hrg.hipster.entity.*;

@HipsterEntity(table="user_table", 
genMongo = TRUE,
genAnnotations = TRUE,
mongoSkipNull = TRUE)
public interface User{

	@Id
	@Column(name="_id")
	public Long getId();
	
	@JoinColumn(name="xxx")
	public String getName();

	public int getAge();
	
	public Address getAddress();
	
	public List<Address> getAddressList();

	public Address[] getAddressArr();
	
	public Boolean[] getArrayBoolean();

	public boolean[] getArrayBooleanP();
	
	public Integer[] getArrayInteger();

	public int[] getArrayInt();
	

}
