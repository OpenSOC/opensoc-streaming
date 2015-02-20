package com.opensoc.parsing.parsers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import oi.thekraken.grok.api.Grok;
import oi.thekraken.grok.api.Match;
import oi.thekraken.grok.api.exception.GrokException;

public class BasicFireEyeParser extends AbstractParser implements Serializable {

	private static final long serialVersionUID = 6328907550159134550L;
	private transient static Grok grok;
	private transient static InputStream pattern_url;
	
	public BasicFireEyeParser() throws Exception {
		pattern_url = getClass().getClassLoader().getResourceAsStream(
				"patterns/fireeye");

		File file = ParserUtils.stream2file(pattern_url);
		grok = Grok.create(file.getPath());
		
		grok.compile("%{FIREEYE_BASE}");
	}

	public BasicFireEyeParser(String filepath) throws GrokException {

		grok = Grok.create(filepath);
		grok.compile("%{FIREEYE_BASE}");

	}
	
	public BasicFireEyeParser(String filepath, String pattern) throws GrokException {

		grok = Grok.create(filepath);
		grok.compile("%{" + pattern + "}");
	}
	
	
	@Override
	public JSONObject parse(byte[] raw_message) {
		String toParse = "";
		JSONObject toReturn;

		try {

			toParse = new String(raw_message, "UTF-8");

			//System.out.println("Received message: " + toParse);

			Match gm = grok.match(toParse);
			gm.captures();

			toReturn = new JSONObject();

			toReturn.putAll(gm.toMap());

			String id = toReturn.get("uid").toString();
			
			// We are not parsing the fedata for multi part message as we cannot determine how we can split the message and how many multi part messages can there be. 
			// The message itself will be stored in the response. 
			
			
			String[] tokens = id.split("\\.");
			if(tokens.length == 1 ) {
		     
			String syslog = toReturn.get("syslog").toString();
			
			Multimap<String, String> multiMap =  formatMain(syslog) ;
			
			for(String key : multiMap.keySet()) {
				
				String value =Joiner.on(",").join( multiMap.get(key));
				toReturn.put(key, value);
			}
			
			}
			
			String ip_src_addr = (String) toReturn.get("dvc");
			String ip_src_port = (String) toReturn.get("src_port");
			String ip_dst_addr = (String) toReturn.get("dst_ip");
			String ip_dst_port = (String) toReturn.get("dst_port");
			
			if(ip_src_addr != null)
				toReturn.put("ip_src_addr", ip_src_addr);
			if(ip_src_port != null)
				toReturn.put("ip_src_port", ip_src_port);
			if(ip_dst_addr != null)
				toReturn.put("ip_dst_addr", ip_dst_addr);
			if(ip_dst_port != null)
				toReturn.put("ip_dst_port", ip_dst_port);
			
			return toReturn;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	
	private Multimap<String, String> formatMain(String in) {
		Multimap<String, String> multiMap = ArrayListMultimap.create();
		String input = in.replaceAll("cn3", "dst_port").replaceAll("cs5", "cncHost").replaceAll("proto","protocol")
						.replaceAll("rt=", "timestamp=").replaceAll("cs1", "malware").replaceAll("dst=", "dst_ip=").replaceAll("shost","src_hostname")
						 .replaceAll("dmac", "dst_mac").replaceAll("smac", "src_mac").replaceAll("spt", "src_port").replaceAll("\\bsrc\\b","src_ip");
		String[] tokens = input.split("\\|");
		
		
		if(tokens.length > 0 ) {
			String message = tokens[tokens.length -1]; 

			String pattern = "([\\w\\d]+)=([^=]*)(?=\\s*\\w+=|\\s*$) ";
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(message);
			
			
			while (m.find()) {
				String[] str = m.group().split("=");
				multiMap.put(str[0] , str[1]);
				
			}
			
			

		}
		return multiMap;
	}

}