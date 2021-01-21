package hr.hrg.hipster.jackson;

import java.io.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.*;
import com.fasterxml.jackson.databind.ser.std.*;

public class DirectSerializer  extends StdSerializer<IDirectSerializerReady>{

	private static final long serialVersionUID = 1L;

	public DirectSerializer() {
		super(IDirectSerializerReady.class);
	}
	
	@Override
	public void serialize(IDirectSerializerReady value, JsonGenerator jgen, SerializerProvider provider)throws IOException {
		value.serialize(jgen, provider);
	}
	
	@Override
	public void serializeWithType(IDirectSerializerReady value, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
		value.serialize(jgen, provider);
	}
	
}
