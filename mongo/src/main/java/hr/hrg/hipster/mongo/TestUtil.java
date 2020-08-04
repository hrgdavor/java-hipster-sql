package hr.hrg.hipster.mongo;

import java.util.*;
import java.util.concurrent.*;

import org.bson.codecs.*;
import org.bson.codecs.configuration.*;
import org.bson.codecs.pojo.*;

import com.mongodb.*;
import com.mongodb.MongoClientSettings.*;
import com.mongodb.client.*;

import hr.hrg.hipster.entity.*;
import hr.hrg.hipster.sql.*;

import static org.bson.codecs.configuration.CodecRegistries.*;
import static com.mongodb.MongoClientSettings.*;

public class TestUtil {
	public static HipsterSql hip = makeHispter();

	public static HipsterSql makeHispter() {
		HipsterSql hipsterSql = new HipsterSql();
		hipsterSql.getEntitySource().register(UserMeta.class);
		hipsterSql.getEntitySource().register(AddressMeta.class);
		return hipsterSql;
	}

	static class MyCodecProvider implements CodecProvider{
		UserMeta userCodec = hip.getEntitySource().getMetaInstance(UserMeta.class);
		AddressMeta addressCodec = hip.getEntitySource().getMetaInstance(AddressMeta.class);
		
		@Override
		public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
			if(User.class.isAssignableFrom(clazz)) return (Codec<T>) userCodec;
			if(Address.class.isAssignableFrom(clazz)) return (Codec<T>) addressCodec;
			return null;
		}
		
	}

	
	static MyCodecProvider myCodecProvider = new MyCodecProvider();
	
	public static final String TEST_BD_NAME = "HipsterMongoTestDb";

	public static MongoClient createMongoClient(MongoCfg cfg) {
		List<ServerAddress> seeds = new ArrayList<>();
		if(cfg.servers != null) for (String server : cfg.servers) {
			int idx = server.indexOf(':');
			if(idx == -1) {				
				seeds.add(new ServerAddress(server));
			}else {
				int port = Integer.parseInt(server.substring(idx+1));
				server = server.substring(0,idx);
				seeds.add(new ServerAddress(server, port));
			}
		}
		
		PojoCodecProvider codecProvider = PojoCodecProvider.builder().automatic(true).build();
		CodecRegistry pojoCodecRegistry = fromRegistries(
				getDefaultCodecRegistry(), 
				fromProviders(new EnumCodecProvider(), myCodecProvider,codecProvider)
			);
		
		for(IEntityMeta<?, ?> meta:hip.getEntitySource()) {
			if(meta instanceof MongoEntityMeta) {
				MongoEntityMeta mongoEntityMeta = (MongoEntityMeta) meta;
				mongoEntityMeta.setCodecRegistry(pojoCodecRegistry);
			}
		}

		Builder builder2 = MongoClientSettings.builder()
		  		.codecRegistry(pojoCodecRegistry)
		       .applyToClusterSettings(builder ->{
		    	   builder.serverSelectionTimeout(1000, TimeUnit.MILLISECONDS);
		           builder.hosts(seeds);
		       });
		if(!cfg.username.isEmpty())
			builder2.credential(MongoCredential.createCredential(cfg.username, cfg.userDb, cfg.password == null ? null: cfg.password.toCharArray()));
		
		return MongoClients.create(builder2.build());	
	}
	
}
