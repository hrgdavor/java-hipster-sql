package hr.hrg.hipster.mongo;
import org.bson.codecs.*;
import org.bson.codecs.configuration.*;

public class EnumCodecProvider implements CodecProvider{

	@Override
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
		
		if(clazz.isEnum()) {
			return new EnumCodec(clazz);
		}

		return null;
	}

}
