package com.opensoc.parsing.parsers;

import java.io.Serializable;
import java.net.URL;

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

	public GrokAsaParser() throws Exception {
		//URL pattern_url = getClass().getClassLoader().getResource(
			//	"patterns/asa");

		grok = Grok.create("/tmp/asa");
		grok.compile("%{CISCO_TAGGED_SYSLOG}");
	}

	public GrokAsaParser(String filepath) throws GrokException {

		grok = Grok.create(filepath);
		grok.compile("%{CISCO_TAGGED_SYSLOG}");

	}

	public GrokAsaParser(String filepath, String pattern) throws GrokException {

		grok = Grok.create(filepath);
		grok.compile("%{" + pattern + "}");
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
