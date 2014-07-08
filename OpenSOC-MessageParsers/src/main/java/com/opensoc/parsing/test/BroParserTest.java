package com.opensoc.parsing.test;

import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.opensoc.parsing.parsers.BasicBroParser;

/**
 * @author kiran
 *
 */
public class BroParserTest {
	/**
	 * @throws ParseException
	 * Test Case for BroParser.
	 * Parses Static json Stirng and checks if any spl chars are present in parsed string.
	 */
	@Test
	public void CheckSpecialChars() throws ParseException {

		String jsonString = "{ \"first_Column\":\"SomeValue\", \"second+Column\":\"someValue\" }";

		BasicBroParser broparser = new BasicBroParser();
		String cleanJson = broparser.parse(jsonString);
		System.out.println(cleanJson);

		Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);

		JSONParser parser = new JSONParser();

		Map json = (Map) parser.parse(cleanJson);
		Map output = new HashMap();
		Iterator iter = json.entrySet().iterator();

		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();

			Matcher m = p.matcher(key);
			boolean b = m.find();
			// Test False
			assertFalse(b);
		}

	}
}
