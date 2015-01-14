package com.opensoc.parsing.parsers;

import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import oi.thekraken.grok.api.Grok;
import oi.thekraken.grok.api.Match;
import oi.thekraken.grok.api.exception.GrokException;

import org.json.simple.JSONObject;

public class GrokAsaParser extends AbstractParser implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private transient static Grok grok;
	Map<String,String> patternMap;
	Map<String,Grok> grokMap;
	URL pattern_url;

	public GrokAsaParser() throws Exception {
		 pattern_url = getClass().getClassLoader().getResource(
				"patterns/asa");

		grok = Grok.create(pattern_url.getFile());
		
		patternMap = getPatternMap();
		grokMap = getGrokMap();
		
		grok.compile("%{CISCO_TAGGED_SYSLOG}");
	}

	public GrokAsaParser(String filepath) throws GrokException {

		grok = Grok.create(filepath);
		//grok.getNamedRegexCollection().put("ciscotag","CISCOFW302013_302014_302015_302016");
		grok.compile("%{CISCO_TAGGED_SYSLOG}");

	}

	public GrokAsaParser(String filepath, String pattern) throws GrokException {

		grok = Grok.create(filepath);
		grok.compile("%{" + pattern + "}");
	}

	
	private Map<String, Object> getMap(String pattern,String text) throws GrokException {
		 
		Grok g = grokMap.get(pattern);
		
		Match gm = g.match(text);
		gm.captures();
		
		
		return gm.toMap();
		
		
	}
	
	private Map<String,Grok> getGrokMap() throws GrokException   {
		Map<String,Grok> map = new HashMap<String,Grok>();
		
		for(Map.Entry<String, String> entry : patternMap.entrySet()  )
			   	{
				Grok grok =  Grok.create(pattern_url.getFile());
				grok.compile("%{"+entry.getValue()+"}");
				
			    map.put(entry.getValue(), grok);
			
			   	}
			
		return map;
	}
	
	private Map<String, String> getPatternMap() {
		Map<String, String> map = new HashMap<String,String>();
		
		
				map.put("ASA-2-106001","CISCOFW106001");
				map.put("ASA-2-106006","CISCOFW106006_106007_106010");
				map.put("ASA-2-106007","CISCOFW106006_106007_106010");
				map.put("ASA-2-106010","CISCOFW106006_106007_106010"); 
				map.put("ASA-3-106014","CISCOFW106014");
				map.put("ASA-6-106015","CISCOFW106015");
				map.put("ASA-1-106021","CISCOFW106021");
				map.put("ASA-4-106023","CISCOFW106023");
				map.put("ASA-5-106100","CISCOFW106100");
				map.put("ASA-6-110002","CISCOFW110002");
				map.put("ASA-6-302010","CISCOFW302010");
				map.put("ASA-6-302013","CISCOFW302013_302014_302015_302016");
				map.put("ASA-6-302014","CISCOFW302013_302014_302015_302016");
				map.put("ASA-6-302015","CISCOFW302013_302014_302015_302016");
				map.put("ASA-6-302016","CISCOFW302013_302014_302015_302016");
				map.put("ASA-6-302020","CISCOFW302020_302021");
				map.put("ASA-6-302021","CISCOFW302020_302021");
				map.put("ASA-6-305011","CISCOFW305011");
				map.put("ASA-3-313001","CISCOFW313001_313004_313008");
				map.put("ASA-3-313004","CISCOFW313001_313004_313008");
				map.put("ASA-3-313008","CISCOFW313001_313004_313008"); 
				map.put("ASA-4-313005","CISCOFW313005");
				map.put("ASA-4-402117","CISCOFW402117");
				map.put("ASA-4-402119","CISCOFW402119");
				map.put("ASA-4-419001","CISCOFW419001");
				map.put("ASA-4-419002","CISCOFW419002");
				map.put("ASA-4-500004","CISCOFW500004");
				map.put("ASA-6-602303","CISCOFW602303_602304");
				map.put("ASA-6-602304","CISCOFW602303_602304");
				map.put("ASA-7-710001","CISCOFW710001_710002_710003_710005_710006");
				map.put("ASA-7-710002","CISCOFW710001_710002_710003_710005_710006");
				map.put("ASA-7-710003","CISCOFW710001_710002_710003_710005_710006");
				map.put("ASA-7-710005","CISCOFW710001_710002_710003_710005_710006");
				map.put("ASA-7-710006","CISCOFW710001_710002_710003_710005_710006"); 
				map.put("ASA-6-713172","CISCOFW713172");
				map.put("ASA-4-733100","CISCOFW733100");

				
		return map;
	}
	
	@Override
	public JSONObject parse(byte[] raw_message) {
		JSONObject payload = new JSONObject();
		String toParse = "";
		JSONObject toReturn;

		try {

			toParse = new String(raw_message, "UTF-8");

			System.out.println("Received message: " + toParse);

			Match gm = grok.match(toParse);
			gm.captures();
			
			toReturn = new JSONObject();
			
			
			toReturn.putAll(gm.toMap());
			
			String str = toReturn.get("ciscotag").toString();
			String pattern = patternMap.get(str);
			
			Map<String, Object> response = getMap(pattern,toParse);
			
			toReturn.putAll(response);


			System.out.println("*******I MAPPED: " + toReturn);
			System.out.println("*******I PATTERNS: " + grok.getPatterns());
			System.out.println("*******I regex: " + grok.getNamedRegex());

			return toReturn;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

}
