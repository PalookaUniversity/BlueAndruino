package com.palookaville.bluelink;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import android.content.Context;

public class Util {
	Context context;
	
	private static Util instance = new Util();
	public static Util getInstance() { return instance; }
	public Util setContext(Context context){
		this.context = context;
		return this;
	}

	
	String driveTest(String s){
    	guaranteeEmptyDirectory("scripts");
    	String data = "blork";
    	guaranteeTextFile("test1","scripts",data);
    	String result = getTextFile("test1","scripts");
    	boolean matched = data.equals(result);
    	return "ok";
	}
	
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
    
    String fullPath(String s){
    	try {
    		return context.getFilesDir().getCanonicalPath() + File.separator + s;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
    }
    
    void guaranteeEmptyDirectory(String s){
    	File dir = guaranteeDirectory(fullPath(s));
    	for (File f : dir.listFiles()){
    		f.delete();
    	}
    	return;
    }
    
    File guaranteeDirectory(String s){
    	File dir = new File(s); 
    	if (!dir.exists()){
    		dir.mkdirs();
    	}
    	return dir;
    }
    
    void guaranteeTextFile(String name, String path, String value){
    	File dir = new File(fullPath(path));
    	if (!dir.exists()){
    		dir.mkdirs();
    	}
    	File file = new File(fullPath(path) + File.separator + name);
		try {
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			fw.write(value);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}   	
    }
    
    String getTextFile(String name, String path){
    	String result = "";
    	File file = new File(path + File.separator + name);
    	StringBuilder sb = new StringBuilder();
    	try {
			BufferedReader br = new BufferedReader(new FileReader(file));
	        String line = "";
	        while ((line = br.readLine()) != null) {
	          sb.append(line +"\n");
	        }
	        result = sb.toString().trim();		}
    	catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}    	
    	return result;
    }
    
    void saveTextFile(String name, String path, String text){
    	
    	File file = new File(path + File.separator + name);    	
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(text);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }   	
    }
}
