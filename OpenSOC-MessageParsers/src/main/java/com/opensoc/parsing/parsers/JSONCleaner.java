package com.opensoc.parsing.parsers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * @author kiran
 *
 */
public class JSONCleaner implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * @param jsonString
	 * @return
	 * @throws ParseException
	 * Takes a json String as input and removes any Special Chars (^ a-z A-Z 0-9) in the keys
	 */
	@SuppressWarnings({"unchecked","rawtypes"})
	public String Clean(String jsonString) throws ParseException
	{
		JSONParser parser = new JSONParser();
		
		
		Map json = (Map) parser.parse(jsonString);
		Map output = new HashMap();
	    Iterator iter = json.entrySet().iterator();

		 while(iter.hasNext()){
		      Map.Entry entry = (Map.Entry)iter.next();
		      
		      String key = ((String)entry.getKey()).replaceAll("[^a-zA-Z0-9]+","");
		      output.put(key, entry.getValue());
		    }

		return JSONValue.toJSONString(output);
	}
	
	
	public static void main(String args[])
	{
		String jsonText = "{\"first_1\": 123, \"second\": [4, 5, 6], \"third\": 789}";
		JSONCleaner cleaner = new JSONCleaner();
		try {
			cleaner.Clean(jsonText);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
}
