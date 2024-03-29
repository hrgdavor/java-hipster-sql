package hr.hrg.hipster.entity;

import java.lang.reflect.*;
import java.util.*;

import org.bson.*;
import org.bson.codecs.*;

import hr.hrg.hipster.sql.*;

public class MongoDecode {

	public static final <T1> List<T1> decodeListMutable(Codec<T1> codec, BsonReader reader, DecoderContext decoderContext) {
		if(reader.getCurrentBsonType() == BsonType.NULL) {reader.readNull(); return null;};
		
		List<T1> list = new ArrayList<T1>();
		
		reader.readStartArray();
		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			list.add(codec.decode(reader, decoderContext));
		}
		reader.readEndArray();
		
		return list;		
	}

	public static final <T1> List<T1> decodeList(Codec<T1> codec, BsonReader reader, DecoderContext decoderContext) {
		return decodeListImmutable(codec, reader, decoderContext);		
	}

	public static final <T1> ImmutableList<T1> decodeListImmutable(Codec<T1> codec, BsonReader reader, DecoderContext decoderContext) {
		List<T1> list = decodeListMutable(codec, reader, decoderContext);
		if(list == null) return null;
		return new ImmutableList<>(list);
	}
	
	public static final <T1> T1[] decodeArray(Codec<T1> codec, BsonReader reader, DecoderContext decoderContext) {
		
		List<T1> list = decodeListMutable(codec, reader, decoderContext);

		if(list == null)  return null;
		
		return list.toArray((T1[]) Array.newInstance(codec.getEncoderClass(), list.size()));
	}
	
	public static final float decodeFloat(BsonReader reader) {
		return decodeFloat(reader, 0);
	}
	
	public static final float decodeFloat(BsonReader reader, float def) {
		BsonType bsonType = reader.getCurrentBsonType();
		if(bsonType == BsonType.INT32) 
			return (float) reader.readInt32();
		else if(bsonType == BsonType.INT64)
			return (float) reader.readInt64();
		else if(bsonType == BsonType.STRING)
			return Float.parseFloat(reader.readString());
		else if(reader.getCurrentBsonType() == BsonType.DOUBLE)
			return (float) reader.readDouble();
		else if(reader.getCurrentBsonType() == BsonType.BOOLEAN)
			return reader.readBoolean() ? 1.0f:0.0f;
		else
			reader.skipValue();
		
		return def;
	}

	public static final Float decodeFloatObject(BsonReader reader){
		return decodeFloatObject(reader, null);
	}
	
	public static final Float decodeFloatObject(BsonReader reader, Float def){
		BsonType bsonType = reader.getCurrentBsonType();
		if(bsonType == BsonType.INT32) 
			return (float) reader.readInt32();
		else if(bsonType == BsonType.INT64)
			return (float) reader.readInt64();
		else if(bsonType == BsonType.STRING)
			return Float.parseFloat(reader.readString());
		else if(reader.getCurrentBsonType() == BsonType.DOUBLE)
			return (float) reader.readDouble();
		else if(reader.getCurrentBsonType() == BsonType.BOOLEAN)
			return reader.readBoolean() ? 1.0f:0.0f;
		else
			reader.skipValue();
		
		return def;
	}	
	
	public static final double decodeDouble(BsonReader reader) {
		return decodeDouble(reader, 0d);
	}
	
	public static final double decodeDouble(BsonReader reader, double def) {
		BsonType bsonType = reader.getCurrentBsonType();
		if(bsonType == BsonType.INT32) 
			return (double) reader.readInt32();
		else if(bsonType == BsonType.INT64)
			return (double) reader.readInt64();
		else if(bsonType == BsonType.STRING)
			return Double.parseDouble(reader.readString());
		else if(reader.getCurrentBsonType() == BsonType.DOUBLE)
			return reader.readDouble();
		else if(reader.getCurrentBsonType() == BsonType.BOOLEAN)
			return reader.readBoolean() ? 1.0:0.0;
		else
			reader.skipValue();
		
		return def;
	}

	public static final Double decodeDoubleObject(BsonReader reader){
		return decodeDoubleObject(reader, null);
	}
	
	public static final Double decodeDoubleObject(BsonReader reader, Double def){
		BsonType bsonType = reader.getCurrentBsonType();
		if(bsonType == BsonType.INT32) 
			return (double) reader.readInt32();
		else if(bsonType == BsonType.INT64)
			return (double) reader.readInt64();
		else if(bsonType == BsonType.STRING)
			return Double.parseDouble(reader.readString());
		else if(reader.getCurrentBsonType() == BsonType.DOUBLE)
			return reader.readDouble();
		else if(reader.getCurrentBsonType() == BsonType.BOOLEAN)
			return reader.readBoolean() ? 1.0:0.0;
		else
			reader.skipValue();
		
		return def;
	}

	public static final int decodeInt(BsonReader reader) {
		return decodeInt(reader, 0);
	}
	public static final int decodeInt(BsonReader reader, int def) {
		BsonType bsonType = reader.getCurrentBsonType();
		if(bsonType == BsonType.INT32) 
			return reader.readInt32();
		else if(bsonType == BsonType.INT64)
			return (int) reader.readInt64();
		else if(bsonType == BsonType.STRING)
			return Integer.parseInt(reader.readString());
		else if(reader.getCurrentBsonType() == BsonType.DOUBLE)
			return (int) reader.readDouble();
		else if(reader.getCurrentBsonType() == BsonType.BOOLEAN)
			return reader.readBoolean() ? 1:0;
		else
			reader.skipValue();
		
		return def;
	}

	public static final Integer decodeIntObject(BsonReader reader){
		return decodeIntObject(reader, null);
	}
	
	public static final Integer decodeIntObject(BsonReader reader, Integer def){
		BsonType bsonType = reader.getCurrentBsonType();
		if(bsonType == BsonType.INT32) 
			return reader.readInt32();
		else if(bsonType == BsonType.INT64)
			return (int) reader.readInt64();
		else if(bsonType == BsonType.STRING)
			return Integer.parseInt(reader.readString());
		else if(reader.getCurrentBsonType() == BsonType.DOUBLE)
			return (int) reader.readDouble();
		else if(reader.getCurrentBsonType() == BsonType.BOOLEAN)
			return reader.readBoolean() ? 1:0;
		else
			reader.skipValue();
		
		return def;
	}
	

	public static final short decodeShort(BsonReader reader) {
		return decodeShort(reader, (short) 0);
	}
	public static final short decodeShort(BsonReader reader, short def) {
		BsonType bsonType = reader.getCurrentBsonType();
		if(bsonType == BsonType.INT32) 
			return (short) reader.readInt32();
		else if(bsonType == BsonType.INT64)
			return (short) reader.readInt64();
		else if(bsonType == BsonType.STRING)
			return Short.parseShort(reader.readString());
		else if(reader.getCurrentBsonType() == BsonType.DOUBLE)
			return (short) reader.readDouble();
		else if(reader.getCurrentBsonType() == BsonType.BOOLEAN)
			return (short) (reader.readBoolean() ? 1:0);
		else
			reader.skipValue();
		
		return def;
	}

	public static final Short decodeShortObject(BsonReader reader){
		return decodeShortObject(reader, null);
	}
	
	public static final Short decodeShortObject(BsonReader reader, Short def){
		BsonType bsonType = reader.getCurrentBsonType();
		if(bsonType == BsonType.INT32) 
			return (short) reader.readInt32();
		else if(bsonType == BsonType.INT64)
			return (short) reader.readInt64();
		else if(bsonType == BsonType.STRING)
			return Short.parseShort(reader.readString());
		else if(reader.getCurrentBsonType() == BsonType.DOUBLE)
			return (short) reader.readDouble();
		else if(reader.getCurrentBsonType() == BsonType.BOOLEAN)
			return (short) (reader.readBoolean() ? 1:0);
		else
			reader.skipValue();
		
		return def;
	}
	

	public static final long decodeLong(BsonReader reader) {
		return decodeLong(reader, 0);
	}
	public static final long decodeLong(BsonReader reader, long def) {
		BsonType bsonType = reader.getCurrentBsonType();
		if(bsonType == BsonType.INT32) 
			return reader.readInt32();
		else if(bsonType == BsonType.INT64)
			return reader.readInt64();
		else if(bsonType == BsonType.STRING)
			return Long.parseLong(reader.readString());
		else if(reader.getCurrentBsonType() == BsonType.DOUBLE)
			return (long) reader.readDouble();
		else if(reader.getCurrentBsonType() == BsonType.BOOLEAN)
			return reader.readBoolean() ? 1L:0L;
		else
			reader.skipValue();
		
		return def;
	}

	public static final Long decodeLongObject(BsonReader reader){
		return decodeLongObject(reader, null);
	}
	
	public static final Long decodeLongObject(BsonReader reader, Long def){
		BsonType bsonType = reader.getCurrentBsonType();
		if(bsonType == BsonType.INT32) 
			return Long.valueOf(reader.readInt32());
		else if(bsonType == BsonType.INT64)
			return reader.readInt64();
		else if(bsonType == BsonType.STRING)
			return Long.parseLong(reader.readString());
		else if(reader.getCurrentBsonType() == BsonType.DOUBLE)
			return (long) reader.readDouble();
		else if(reader.getCurrentBsonType() == BsonType.BOOLEAN)
			return reader.readBoolean() ? 1L:0L;
		else
			reader.skipValue();
		
		return def;
	}	
	
	public static final boolean decodeBoolean(BsonReader reader) {
		return decodeBoolean(reader, false);
	}
	
	public static final boolean decodeBoolean(BsonReader reader, boolean def) {
		BsonType bsonType = reader.getCurrentBsonType();
		if(reader.getCurrentBsonType() == BsonType.BOOLEAN)
			return reader.readBoolean();
		else if(bsonType == BsonType.INT32) 
			return reader.readInt32() != 0;
		else if(bsonType == BsonType.INT64)
			return (int) reader.readInt64() != 0;
		else if(bsonType == BsonType.STRING)
			return "true".equalsIgnoreCase(reader.readString());
		else if(reader.getCurrentBsonType() == BsonType.DOUBLE)
			return reader.readDouble() != 0.0;
		else
			reader.skipValue();
	
		return def;
	}

	public static final Boolean decodeBooleanObject(BsonReader reader){
		return decodeBooleanObject(reader, null);
	}
	public static final Boolean decodeBooleanObject(BsonReader reader, Boolean def){
		BsonType bsonType = reader.getCurrentBsonType();
		if(reader.getCurrentBsonType() == BsonType.BOOLEAN)
			return reader.readBoolean();
		else if(bsonType == BsonType.INT32) 
			return reader.readInt32() != 0;
		else if(bsonType == BsonType.INT64)
			return (int) reader.readInt64() != 0;
		else if(bsonType == BsonType.STRING)
			return "true".equalsIgnoreCase(reader.readString());
		else if(reader.getCurrentBsonType() == BsonType.DOUBLE)
			return reader.readDouble() != 0.0;
		else
			reader.skipValue();
		
		return def;
	}

	
	/* **** String ***** */
	
	public static final  List<String> decodeListStringMutable(BsonReader reader, DecoderContext decoderContext) {
		if(reader.getCurrentBsonType() == BsonType.NULL)  {reader.readNull(); return null;}
		
		List<String> list = new ArrayList<>();
		
		reader.readStartArray();
		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			list.add(decodeString(reader, decoderContext));				
		}
		reader.readEndArray();
		
		return list;		
	}

	public static final List<String> decodeListString(BsonReader reader, DecoderContext decoderContext) {
		return decodeListStringMutable(reader, decoderContext); // TODO immutable		
	}
	
	public static final String[] decodeArrayString(BsonReader reader, DecoderContext decoderContext) {
		
		List<String> list = decodeListStringMutable(reader, decoderContext);

		if(list == null)  return null;
		
		return list.toArray(new String[list.size()]);
	}

	
	/* **** Boolean ***** */	
	
	public static final  List<Boolean> decodeListBooleanMutable(BsonReader reader, DecoderContext decoderContext) {
		if(reader.getCurrentBsonType() == BsonType.NULL)  {reader.readNull(); return null;}
		
		List<Boolean> list = new ArrayList<>();
		
		reader.readStartArray();
		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			if(reader.getCurrentBsonType() == BsonType.BOOLEAN) {
				list.add(reader.readBoolean());				
			}else {
				reader.skipValue();
			}			
		}
		reader.readEndArray();
		
		return list;		
	}

	public static final List<Boolean> decodeListBoolean(BsonReader reader, DecoderContext decoderContext) {
		return decodeListBooleanMutable(reader, decoderContext); // TODO immutable		
	}
	
	public static final Boolean[] decodeArrayBoolean(BsonReader reader, DecoderContext decoderContext) {
		
		List<Boolean> list = decodeListBooleanMutable(reader, decoderContext);

		if(list == null)  return null;
		
		return list.toArray(new Boolean[list.size()]);
	}

	public static final boolean[] decodeArrayBooleanPrimitive(BsonReader reader, DecoderContext decoderContext) {
		
		List<Boolean> list = decodeListBooleanMutable(reader, decoderContext);

		if(list == null) return null;
		boolean[] arr = new boolean[list.size()];
		
		for (int i = 0; i < arr.length; i++) {
			Boolean value = list.get(i);
			if(value != null) arr[i] = value; 
		}
		return arr;
	}

	
	/* **** Float ***** */
	
	
	public static final  List<Float> decodeListFloatMutable(BsonReader reader, DecoderContext decoderContext) {
		if(reader.getCurrentBsonType() == BsonType.NULL)  {reader.readNull(); return null;}
		
		List<Float> list = new ArrayList<>();
		
		reader.readStartArray();
		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			BsonType bsonType = reader.getCurrentBsonType();
			if(bsonType == BsonType.INT32) 
				list.add((float) reader.readInt32());
			else if(bsonType == BsonType.INT64)
				list.add((float) reader.readInt64());
			else if(bsonType == BsonType.STRING)
				list.add(Float.parseFloat(reader.readString()));
			else if(reader.getCurrentBsonType() == BsonType.DOUBLE)
				list.add((float)reader.readDouble());
			else
				reader.skipValue();
		}
		reader.readEndArray();
		
		return list;		
	}

	public static final List<Float> decodeListFloat(BsonReader reader, DecoderContext decoderContext) {
		return decodeListFloatMutable(reader, decoderContext); // TODO immutable		
	}
	
	public static final Float[] decodeArrayFloat(BsonReader reader, DecoderContext decoderContext) {
		
		List<Float> list = decodeListFloatMutable(reader, decoderContext);

		if(list == null)  return null;
		
		return list.toArray(new Float[list.size()]);
	}

	public static final float[] decodeArrayFloatPrimitive(BsonReader reader, DecoderContext decoderContext) {
		
		List<Float> list = decodeListFloatMutable(reader, decoderContext);

		if(list == null)  return null;
		
		float[] arr = new float[list.size()];
		
		for (int i = 0; i < arr.length; i++) {
			Float value = list.get(i);
			if(value != null) arr[i] = value; 
		}
		return arr;
	}

	/* **** Double *** */
	
	public static final  List<Double> decodeListDoubleMutable(BsonReader reader, DecoderContext decoderContext) {
		if(reader.getCurrentBsonType() == BsonType.NULL)  {reader.readNull(); return null;}
		
		List<Double> list = new ArrayList<>();
		
		reader.readStartArray();
		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {

		}
		reader.readEndArray();
		
		return list;		
	}

	public static final List<Double> decodeListDouble(BsonReader reader, DecoderContext decoderContext) {
		return decodeListDoubleMutable(reader, decoderContext); // TODO immutable		
	}
	
	public static final Double[] decodeArrayDouble(BsonReader reader, DecoderContext decoderContext) {
		
		List<Double> list = decodeListDoubleMutable(reader, decoderContext);

		if(list == null) return null;
		
		return list.toArray(new Double[list.size()]);
	}

	public static final double[] decodeArrayDoublePrimitive(BsonReader reader, DecoderContext decoderContext) {
		
		List<Double> list = decodeListDoubleMutable(reader, decoderContext);

		if(list == null) return null;
		
		double[] arr = new double[list.size()];
		
		for (int i = 0; i < arr.length; i++) {
			Double value = list.get(i);
			if(value != null) arr[i] = value; 
		}
		return arr;
	}
	

	/* **** Long ***** */
	
	
	
	public static final  List<Long> decodeListLongMutable(BsonReader reader, DecoderContext decoderContext) {
		if(reader.getCurrentBsonType() == BsonType.NULL)  {reader.readNull(); return null;}
		
		List<Long> list = new ArrayList<>();
		
		reader.readStartArray();
		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			BsonType bsonType = reader.getCurrentBsonType();
			if(bsonType == BsonType.INT32) 
				list.add((long) reader.readInt32());
			else if(bsonType == BsonType.STRING)
				list.add(Long.parseLong(reader.readString()));
			else if(reader.getCurrentBsonType() == BsonType.INT64)
				list.add(reader.readInt64());
			else
				reader.skipValue();
		}
		reader.readEndArray();
		
		return list;		
	}

	public static final List<Long> decodeListLong(BsonReader reader, DecoderContext decoderContext) {
		return decodeListLongMutable(reader, decoderContext); // TODO immutable		
	}
	
	public static final Long[] decodeArrayLong(BsonReader reader, DecoderContext decoderContext) {
		
		List<Long> list = decodeListLongMutable(reader, decoderContext);

		if(list == null) return null;
		
		return list.toArray(new Long[list.size()]);
	}

	public static final long[] decodeArrayLongPrimitive(BsonReader reader, DecoderContext decoderContext) {
		
		List<Long> list = decodeListLongMutable(reader, decoderContext);

		if(list == null) return null;
		
		long[] arr = new long[list.size()];
		
		for (int i = 0; i < arr.length; i++) {
			Long value = list.get(i);
			if(value != null) arr[i] = value; 
		}
		return arr;
	}

	/* **** Integer ***** */
	
	
	
	public static final  List<Integer> decodeListIntegerMutable(BsonReader reader, DecoderContext decoderContext) {
		if(reader.getCurrentBsonType() == BsonType.NULL)  {reader.readNull(); return null;}
		
		List<Integer> list = new ArrayList<>();
		
		reader.readStartArray();
		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			BsonType bsonType = reader.getCurrentBsonType();
			if(bsonType == BsonType.INT64) 
				list.add((int) reader.readInt64());
			else if(bsonType == BsonType.STRING)
				list.add(Integer.parseInt(reader.readString()));
			else if(reader.getCurrentBsonType() == BsonType.INT32)
				list.add(reader.readInt32());
			else
				reader.skipValue();
		}
		reader.readEndArray();
		
		return list;		
	}

	public static final List<Integer> decodeListInteger(BsonReader reader, DecoderContext decoderContext) {
		return decodeListIntegerMutable(reader, decoderContext); // TODO immutable		
	}
	
	public static final Integer[] decodeArrayInteger(BsonReader reader, DecoderContext decoderContext) {
		
		List<Integer> list = decodeListIntegerMutable(reader, decoderContext);

		if(list == null) return null;
		
		return list.toArray(new Integer[list.size()]);
	}

	public static final int[] decodeArrayIntegerPrimitive(BsonReader reader, DecoderContext decoderContext) {
		
		List<Integer> list = decodeListIntegerMutable(reader, decoderContext);

		if(list == null) return null;
		
		int[] arr = new int[list.size()];
		
		for (int i = 0; i < arr.length; i++) {
			Integer value = list.get(i);
			if(value != null) arr[i] = value; 
		}
		return arr;
	}


	/* **** Short ***** */	
	
	public static final  List<Short> decodeListShortMutable(BsonReader reader, DecoderContext decoderContext) {
		if(reader.getCurrentBsonType() == BsonType.NULL)  {reader.readNull(); return null;}
		
		List<Short> list = new ArrayList<>();
		
		reader.readStartArray();
		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			BsonType bsonType = reader.getCurrentBsonType();
			if(bsonType == BsonType.INT64) 
				list.add((short) reader.readInt64());
			else if(bsonType == BsonType.STRING)
				list.add(Short.parseShort(reader.readString()));
			else if(reader.getCurrentBsonType() == BsonType.INT32)
				list.add((short)reader.readInt32());
			else
				reader.skipValue();
		}
		reader.readEndArray();
		
		return list;		
	}

	public static final List<Short> decodeListShort(BsonReader reader, DecoderContext decoderContext) {
		return decodeListShortMutable(reader, decoderContext); // TODO immutable		
	}
	
	public static final Short[] decodeArrayShort(BsonReader reader, DecoderContext decoderContext) {
		
		List<Short> list = decodeListShortMutable(reader, decoderContext);

		if(list == null) return null;
		
		return list.toArray(new Short[list.size()]);
	}

	public static final short[] decodeArrayShortPrimitive(BsonReader reader, DecoderContext decoderContext) {
		
		List<Short> list = decodeListShortMutable(reader, decoderContext);

		if(list == null) return null;
		
		short[] arr = new short[list.size()];
		
		for (int i = 0; i < arr.length; i++) {
			Short value = list.get(i);
			if(value != null) arr[i] = value; 
		}
		return arr;
	}

	public static String decodeString(BsonReader reader) {
		return decodeString(reader, null);
	}

    public static String decodeString(BsonReader reader, DecoderContext decoderContext) {
    	BsonType currentBsonType = reader.getCurrentBsonType();
    	switch (currentBsonType) {
		case BOOLEAN: return reader.readBoolean() ? "true":"false";
		case DATE_TIME: return Long.toString( reader.readDateTime() );
		case DB_POINTER: reader.skipValue(); return null;
		case DECIMAL128: return reader.readDecimal128().toString();
		case DOUBLE: return Double.toString(reader.readDouble());
		case INT32: return Integer.toString(reader.readInt32());
		case INT64: return Long.toString(reader.readInt64());
		case JAVASCRIPT: reader.skipValue(); return null;
		case JAVASCRIPT_WITH_SCOPE: return null;
		case MAX_KEY: reader.skipValue(); return null;
		case MIN_KEY: reader.skipValue(); return null;
		case NULL: reader.skipValue(); return null;
		case OBJECT_ID: return reader.readObjectId().toString();
		case REGULAR_EXPRESSION: return reader.readRegularExpression().toString();
		case STRING: return reader.readString();
		case SYMBOL: return reader.readSymbol().toString();
		case TIMESTAMP: return reader.readTimestamp().asNumber().toString();
		case UNDEFINED: reader.skipValue(); return null;
		case END_OF_DOCUMENT:
		default:
			throw new RuntimeException("Invalid state "+currentBsonType.name());
		}
    }

}
