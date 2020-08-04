package hr.hrg.hipster.mongo;

import java.util.*;

import org.bson.*;
import org.bson.codecs.*;
import org.bson.codecs.configuration.*;

import hr.hrg.hipster.entity.*;

public class UserCodec implements Codec<User>{

	protected Codec<?>[] _codecs;
	private CodecRegistry _codecRegistry;
	private UserMeta meta;

	public UserCodec(UserMeta meta) {
		this.meta = meta;
		_codecs = new Codec[meta.getColumnCount()];
	}
	
	public void setCodecRegistry(CodecRegistry registry) {
		this._codecRegistry = registry;
		_codecs[3] = registry.get(Address.class);
		_codecs[4] = registry.get(Address.class);
		_codecs[5] = registry.get(Address.class);
	}

	@Override
	public void encode(BsonWriter writer, User value, EncoderContext encoderContext) {
		
		if(value == null) {
			writer.writeNull();
			return;
		}
		
//		double num = 0;
		writer.writeStartDocument();

		writer.writeInt64("_id", value.getId());
				
		writer.writeName("name");
		if(value.getName() == null) 
	    	writer.writeNull(); 
	    else
    		writer.writeString(value.getName());
	    
	    writer.writeInt32("age", value.getAge());
	    
	    writer.writeName("address");	    
	    if (value.getAddress() == null)
	    	writer.writeNull();
	    else{
	    	((Codec<Address>)_codecs[3]).encode(writer, value.getAddress(),encoderContext);
	    }
	    
	    writer.writeName("addressList");
	    MongoEncode.encodeList((Codec<Address>)_codecs[4],writer,value.getAddressList(), encoderContext);
	    
	    writer.writeName("addressArr");
	    MongoEncode.encodeArray((Codec<Address>)_codecs[5],writer,value.getAddressArr(), encoderContext);

	    writer.writeEndDocument();

	}

	@Override
	public Class<User> getEncoderClass() {
		return User.class;
	}

	@Override
	public User decode(BsonReader reader, DecoderContext decoderContext) {
		
		Long id = null;
		String name = null;
		int age = 0;
	    Address address = null;
	    List<Address> addressList = null;
	    Address[] addressArr = null;
	    if(reader.getCurrentBsonType() == BsonType.NULL) return null;
        reader.readStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            
        	String fieldName = reader.readName();
            ColumnMeta<?> column = meta.getColumn(fieldName);
            
            if(column == null && "_id".equals(fieldName)) column = meta.getColumn(0);
            
            if(column != null) {
            	switch (column.ordinal()) {
            	case 0: id = reader.readInt64(); break;
				case 1: name = reader.readString(); break;
				case 2: age = reader.readInt32(); break;
			    case 3: address = ((Codec<Address>)_codecs[3]).decode(reader, decoderContext);break;
			    case 4: addressList = MongoDecode.decodeList( (Codec<Address>) _codecs[5] , reader, decoderContext); break;
//			    		((Codec<List<Address>>)_codecs[4]).decode(reader, decoderContext);break;
			    case 5: addressArr = MongoDecode.decodeArray( (Codec<Address>) _codecs[5] , reader, decoderContext); break; 
//			    		((Codec<Address[]>)_codecs[5]).decode(reader, decoderContext);break;
				default:
					reader.skipValue();
				}
            }else{
            	reader.skipValue();
            	
            }
        }

        
        reader.readEndDocument();

		return new UserImmutable(id,name,age, null,null,null, null, null, null, null);
	}

}
