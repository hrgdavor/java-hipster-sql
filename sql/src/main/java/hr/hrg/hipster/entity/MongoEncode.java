package hr.hrg.hipster.entity;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import org.bson.*;
import org.bson.codecs.*;
import org.bson.codecs.configuration.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

public class MongoEncode {

	
	public static final <T1> void encodeList(Codec<T1> codec, BsonWriter writer, Iterable<T1>list, EncoderContext encoderContext) {
		if(list == null) {
			writer.writeNull();
		}else {
			writer.writeStartArray();
			for(T1 value:list) codec.encode(writer, value, encoderContext);
			writer.writeEndArray();
		}
	}

	public static final <T1> void encodeArray(Codec<T1> codec, BsonWriter writer, T1[] arr, EncoderContext encoderContext) {
		if(arr == null) {
			writer.writeNull();
		}else {
			writer.writeStartArray();
			for(T1 value:arr) codec.encode(writer, value, encoderContext);
			writer.writeEndArray();
		}
	}
	
	/* **** String ***** */
	
	public static final void encodeListString(BsonWriter writer, Iterable<String>list) {
		if(list == null) {
			writer.writeNull();
		}else {
			writer.writeStartArray();
			for(String value:list) writer.writeString(value);
			writer.writeEndArray();
		}
	}
	
	public static final void encodeArray(BsonWriter writer, String[] arr) {
		if(arr == null) {
			writer.writeNull();
		}else {
			writer.writeStartArray();
			for(String value:arr) writer.writeString(value);
			writer.writeEndArray();
		}
	}

	
	/* **** Boolean ***** */	
	
	public static final void encodeListBoolean(BsonWriter writer, Iterable<Boolean>list, EncoderContext encoderContext) {
		if(list == null) {
			writer.writeNull();
		}else {
			writer.writeStartArray();
			for(Boolean value:list) writer.writeBoolean(value);
			writer.writeEndArray();
		}
	}
	
	public static final void encodeArray(BsonWriter writer, boolean[] arr) {
		if(arr == null) {
			writer.writeNull();
		}else {
			writer.writeStartArray();
			for(boolean value:arr) writer.writeBoolean(value);
			writer.writeEndArray();
		}
	}
	
	public static final void encodeArray(BsonWriter writer, Boolean[] arr) {
		if(arr == null) {
			writer.writeNull();
		}else {
			writer.writeStartArray();
			for(Boolean value:arr) writer.writeBoolean(value);
			writer.writeEndArray();
		}
	}
	
	/* **** Float ***** */
	
	public static final void encodeListFloat(BsonWriter writer, Iterable<Float>list, EncoderContext encoderContext) {
		if(list == null) {
			writer.writeNull();
		}else {
			writer.writeStartArray();
			for(Float value:list) writer.writeDouble(value);
			writer.writeEndArray();
		}
	}

	public static final void encodeArray(BsonWriter writer, float[] arr) {
		if(arr == null) {
			writer.writeNull();
		}else {
			writer.writeStartArray();
			for(float value:arr) writer.writeDouble(value);
			writer.writeEndArray();
		}
	}
	
	public static final void encodeArray(BsonWriter writer, Float[] arr) {
		if(arr == null) {
			writer.writeNull();
		}else {
			writer.writeStartArray();
			for(Float value:arr) writer.writeDouble(value);
			writer.writeEndArray();
		}
	}

	/* **** Double ***** */
	
	public static final void encodeListDouble(BsonWriter writer, Iterable<Double>list, EncoderContext encoderContext) {
		if(list == null) {
			writer.writeNull();
		}else {
			writer.writeStartArray();
			for(Double value:list) writer.writeDouble(value);
			writer.writeEndArray();
		}
	}

	public static final void encodeArray(BsonWriter writer, Double[] arr) {
		if(arr == null) {
			writer.writeNull();
		}else {
			writer.writeStartArray();
			for(Double value:arr) writer.writeDouble(value);
			writer.writeEndArray();
		}
	}
	
	public static final void encodeArray(BsonWriter writer, double[] arr) {
		if(arr == null) {
			writer.writeNull();
		}else {
			writer.writeStartArray();
			for(Double value:arr) writer.writeDouble(value);
			writer.writeEndArray();
		}
	}

	/* **** Long ***** */
	
	public static final void encodeListLong(BsonWriter writer, Iterable<Long>list, EncoderContext encoderContext) {
		if(list == null) {
			writer.writeNull();
		}else {
			writer.writeStartArray();
			for(Long value:list) writer.writeInt64(value);
			writer.writeEndArray();
		}
	}

	public static final void encodeArray(BsonWriter writer, long[] arr) {
		if(arr == null) {
			writer.writeNull();
		}else {
			writer.writeStartArray();
			for(long value:arr) writer.writeInt64(value);
			writer.writeEndArray();
		}
	}
	
	public static final void encodeArray(BsonWriter writer, Long[] arr) {
		if(arr == null) {
			writer.writeNull();
		}else {
			writer.writeStartArray();
			for(Long value:arr) writer.writeInt64(value);
			writer.writeEndArray();
		}
	}

	/* **** Integer ***** */
	
	public static final void encodeListInteger(BsonWriter writer, Iterable<Integer>list, EncoderContext encoderContext) {
		if(list == null) {
			writer.writeNull();
		}else {
			writer.writeStartArray();
			for(Integer value:list) writer.writeInt32(value);
			writer.writeEndArray();
		}
	}

	public static final void encodeArray(BsonWriter writer, int[] arr) {
		if(arr == null) {
			writer.writeNull();
		}else {
			writer.writeStartArray();
			for(int value:arr) writer.writeInt32(value);
			writer.writeEndArray();
		}
	}
	
	public static final void encodeArray(BsonWriter writer, Integer[] arr) {
		if(arr == null) {
			writer.writeNull();
		}else {
			writer.writeStartArray();
			for(Integer value:arr) writer.writeInt32(value);
			writer.writeEndArray();
		}
	}

	/* **** Short ***** */
	
	public static final void encodeListShort(BsonWriter writer, Iterable<Short>list) {
		if(list == null) {
			writer.writeNull();
		}else {
			writer.writeStartArray();
			for(Short value:list) writer.writeInt32(value);
			writer.writeEndArray();
		}
	}

	public static final void encodeArray(BsonWriter writer, short[] arr) {
		if(arr == null) {
			writer.writeNull();
		}else {
			writer.writeStartArray();
			for(short value:arr) writer.writeInt32(value);
			writer.writeEndArray();
		}
	}
	
	public static final void encodeArray(BsonWriter writer, Short[] arr) {
		if(arr == null) {
			writer.writeNull();
		}else {
			writer.writeStartArray();
			for(Short value:arr) writer.writeInt32(value);
			writer.writeEndArray();
		}
	}
	
	public static void encodeKeepObjectNode(ObjectMapper mapper, ObjectNode node, BsonWriter writer, EncoderContext context, CodecRegistry registry) {
		if(node == null) return;
		Iterator<Entry<String, JsonNode>> fields = node.fields();
		while (fields.hasNext()) {
			Map.Entry<java.lang.String, com.fasterxml.jackson.databind.JsonNode> entry = (Map.Entry<java.lang.String, com.fasterxml.jackson.databind.JsonNode>) fields
					.next();

		    writer.writeName(entry.getKey());
		    JsonNode value = entry.getValue();
		    
		    if(value == null || value.isNull()) {
		    	writer.writeNull();
		    }else if(value.isTextual()){
		    	writer.writeString(value.asText());
		    }else if(value.isFloatingPointNumber()){
		    	writer.writeDouble(value.asDouble());
		    }else if(value.isBoolean()){
		    	writer.writeBoolean(value.asBoolean());
		    }else if(value.isIntegralNumber()){
		    	long asLong = value.asLong();
		    	if(value.canConvertToInt()) {
		    		writer.writeInt32(value.asInt());		    		
		    	}else {		    		
		    		writer.writeInt64(asLong);
		    	}
		    }else if(value.isPojo()){
		    	Object _val = ((POJONode)value).getPojo();
		    	Codec codec = registry.get(_val.getClass());
		    	if(codec == null) throw new RuntimeException("kept field "+entry.getKey()+" is type "+_val.getClass()+" and has no codec ");
		    	codec.encode(writer, value, context);
		    }else {
		    	Codec codec = registry.get(value.getClass());
		    	if(codec == null) throw new RuntimeException("kept field "+entry.getKey()+" is type "+value.getClass()+" and has no codec ");
		    	codec.encode(writer, value, context);
		    }
		}
	}

	public static void encodeKeepMap(Map<?,?> map, BsonWriter writer, EncoderContext context, CodecRegistry registry) throws IOException {
		if(map == null) return;
		for(Entry<?, ?> entry : map.entrySet()) {
		    writer.writeName(entry.getKey().toString());
			Object value = entry.getValue();
		    if(value == null) {
		    	writer.writeNull();
		    }else{
		    	Object _val = ((POJONode)value).getPojo();
		    	Codec codec = registry.get(_val.getClass());
		    	if(codec == null) throw new RuntimeException("kept field "+entry.getKey()+" is type "+_val.getClass()+" and has no codec ");
		    	codec.encode(writer, value, context);
		    }
		}
	}
}
