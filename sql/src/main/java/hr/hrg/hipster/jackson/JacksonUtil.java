package hr.hrg.hipster.jackson;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

public class JacksonUtil {

	public static void writeKeepObjectNode(ObjectNode node, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		if(node == null) return;
		Iterator<Entry<String, JsonNode>> fields = node.fields();
		while (fields.hasNext()) {
			Map.Entry<java.lang.String, com.fasterxml.jackson.databind.JsonNode> entry = (Map.Entry<java.lang.String, com.fasterxml.jackson.databind.JsonNode>) fields
					.next();

			jgen.writeObjectField(entry.getKey(), entry.getValue());			
		}
	}

	public static void writeKeepMap(Map<?,?> map, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		if(map == null) return;
		for(Entry<?, ?> entry : map.entrySet()) {
			jgen.writeObjectField(entry.getKey().toString(), entry.getValue());			
		}
	}
}
