package hr.hrg.hipster.dao.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

public interface IDirectSerializerReady {

	public void serialize( JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException;
	
}
