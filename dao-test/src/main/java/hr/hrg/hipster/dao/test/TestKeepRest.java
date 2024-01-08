package hr.hrg.hipster.dao.test;

import java.io.*;
import java.util.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

import hr.hrg.hipster.dao.test.entity.*;

public class TestKeepRest {

	static ObjectMapper mapper = new ObjectMapper();
	
	public static void main(String[] args) throws IOException {
		ObjectNode rest = mapper.createObjectNode();
		rest.put("v1", 1);
		rest.put("str1", "BLA");
		UserImmutable user = new UserImmutable(1l, Arrays.asList("jozo"), 24, false, rest);
		
		System.err.println(mapper.writeValueAsString(user));
	}
}
