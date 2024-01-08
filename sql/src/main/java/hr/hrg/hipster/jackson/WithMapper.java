package hr.hrg.hipster.jackson;

import com.fasterxml.jackson.databind.*;

public interface WithMapper {
	public ObjectMapper getMapper();
	public void setMapper(ObjectMapper mapper);
}
