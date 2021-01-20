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
		return new ImmutableList<>(decodeListMutable(codec, reader, decoderContext));
	}
	
	public static final <T1> T1[] decodeArray(Codec<T1> codec, BsonReader reader, DecoderContext decoderContext) {
		
		List<T1> list = decodeListMutable(codec, reader, decoderContext);

		if(list == null)  return null;
		
		return list.toArray((T1[]) Array.newInstance(codec.getEncoderClass(), list.size()));
	}
	
	/* **** String ***** */
	
	public static final  List<String> decodeListStringMutable(BsonReader reader, DecoderContext decoderContext) {
		if(reader.getCurrentBsonType() == BsonType.NULL)  {reader.readNull(); return null;}
		
		List<String> list = new ArrayList<>();
		
		reader.readStartArray();
		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			if(reader.getCurrentBsonType() == BsonType.STRING) {
				list.add(reader.readString());				
			}else {
				reader.skipValue();
			}
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
			BsonType bsonType = reader.getCurrentBsonType();
			if(bsonType == BsonType.INT32) 
				list.add((double) reader.readInt32());
			else if(bsonType == BsonType.INT64)
				list.add((double) reader.readInt64());
			else if(bsonType == BsonType.STRING)
				list.add(Double.parseDouble(reader.readString()));
			else if(reader.getCurrentBsonType() == BsonType.DOUBLE)
				list.add(reader.readDouble());
			else
				reader.skipValue();
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

	
	
}
