package hr.hrg.hipster.mongo;

import static hr.hrg.hipster.sql.BooleanEnum.*;

import hr.hrg.hipster.entity.*;

@HipsterEntity(genMongo = TRUE)
public interface Address {

	public String getStreet();	
	public int getNum();

}
