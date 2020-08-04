package hr.hrg.hipster.mongo;

import java.util.*;

import org.slf4j.*;

import com.mongodb.client.*;

public class InitialMongoPlayTest {
	
	Logger log = LoggerFactory.getLogger(InitialMongoPlayTest.class);
	
	public static void main(String[] args) {
		MongoCfg cfg = new MongoCfg();
		
		LoggerFactory.class.getName();
		
		if(args.length > 0) {
			cfg.servers = new ArrayList<>();
			cfg.servers.add(args[0]);
		}
		if(args.length > 1) cfg.username = args[1]; 
		if(args.length > 2) cfg.password = args[2]; 
		if(args.length > 3) cfg.userDb   = args[3]; 
		
		MongoClient mongoClient = TestUtil.createMongoClient(cfg);
		MongoDatabase database = mongoClient.getDatabase(TestUtil.TEST_BD_NAME);
		
		MongoCollection<User> collection = database.getCollection("tsest1", User.class);
		
		Long id = new Random().nextLong();
		int age = new Random().nextInt(99);
		
		Address address = new AddressImmutable("Ulica", new Random().nextInt(100));
		List<Address> addressLit = Arrays.asList(address);
		Address[] addressArr = new Address[] {address};
		collection.insertOne(new UserImmutable(id,"Jozo "+(new Random().nextLong()), age, address, addressLit, addressArr, 
				new Boolean[] {Boolean.TRUE},// Boolean[] 
				new boolean[] {false},// boolean[]
				new Integer[] {1},// Integer[] 
				new int[] {2} //int[]
				));
		
		FindIterable<User> find = collection.find();
		for (User user : find) {
			System.out.println(user.getId()+" "+user.getName()+" "+user.getAge());
		}
	}
	
}
