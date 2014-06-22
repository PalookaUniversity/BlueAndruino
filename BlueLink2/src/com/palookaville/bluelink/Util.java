package com.palookaville.bluelink;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

public class Util {
	
    @SuppressWarnings("unchecked")
	Map<String,String>jsonToMap(String jsonString) throws IOException {
    	
    	//ObjectMapper mapper = new ObjectMapper();
    	//Map<String,String> map = mapper.readValue(jsonString, HashMap.class);
    	ObjectMapper objectMapper = new ObjectMapper();
        HashMap<String,String> result = objectMapper.readValue(jsonString, HashMap.class);
        return result;
    }
//
    String mapToJson(Map<String,String>map) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        StringWriter stringWriter = new StringWriter();
        objectMapper.writeValue(stringWriter, map);
        return stringWriter.toString();
    }

}
