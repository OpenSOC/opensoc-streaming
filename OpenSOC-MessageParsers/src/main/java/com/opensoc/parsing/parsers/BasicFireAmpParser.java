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

public class BasicFireAmpParser extends AbstractParser implements Serializable {

	private static final long serialVersionUID = -4001084012653107344L;
	private transient static Grok grok;
	private transient static InputStream pattern_url;
	
	public BasicFireAmpParser() throws Exception {
		pattern_url = getClass().getClassLoader().getResourceAsStream(
				"patterns/fireamp");

		File file = ParserUtils.stream2file(pattern_url);
		grok = Grok.create(file.getPath());
		
		grok.compile("%{FIREAMP}");
	}

	public BasicFireAmpParser(String filepath) throws GrokException {

		grok = Grok.create(filepath);
		grok.compile("%{FIREAMP}");

	}
	
	public BasicFireAmpParser(String filepath, String pattern) throws GrokException {

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

			
			String ip_src_addr = (String) toReturn.get("src_ip");
			String ip_dst_addr = (String) toReturn.get("dest_ip");
			
			if(ip_src_addr != null)
				toReturn.put("ip_src_addr", ip_src_addr);
			if(ip_dst_addr != null)
				toReturn.put("ip_dst_addr", ip_dst_addr);
			
			toReturn.put("original_string", toParse);
			
			return toReturn;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	
	
}