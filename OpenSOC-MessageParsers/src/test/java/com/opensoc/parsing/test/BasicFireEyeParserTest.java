/**
 * 
 */
package com.opensoc.parsing.test;



import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.opensoc.parsing.parsers.BasicFireEyeParser;
import com.opensoc.parsing.parsers.BasicSourcefireParser;

/**
 * <ul>
 * <li>Title: Test For SourceFireParser</li>
 * <li>Description: </li>
 * <li>Created: July 8, 2014</li>
 * </ul>
 * @version $Revision: 1.0 $
 */
public class BasicFireEyeParserTest extends TestCase
	{

	//private  static String sourceFireString = "";
	private static String[] inputStrings;
	private BasicFireEyeParser parser=null;



	/**
	 * @throws java.lang.Exception
	 */
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	public static void tearDownAfterClass() throws Exception {
		setInputStrings(null);
	}

	/**
	 * @throws java.lang.Exception
	 */
	public void setUp() throws Exception {
		setInputStrings(new String[] {
				"<164>fenotify-851983.alert: CEF:0|FireEye|CMS|7.2.1.244420|DM|domain-match|1|rt=Feb 09 2015 12:28:26 UTC dvc=10.201.78.57 cn3Label=cncPort cn3=53 cn2Label=sid cn2=80494706 shost=dev001srv02.example.com proto=udp cs5Label=cncHost cs5=mfdclk001.org dvchost=DEVFEYE1 spt=54527 dvc=10.100.25.16 smac=00:00:0c:07:ac:00 cn1Label=vlan cn1=0 externalId=851983 cs4Label=link cs4=https://DEVCMS01.example.com/event_stream/events_for_bot?ev_id\\=851983 dmac=00:1d:a2:af:32:a1 cs1Label=sname cs1=Trojan.Generic.DNS ",
				"<164>fenotify-851987.alert: CEF:0|FireEye|CMS|7.2.1.244420|DM|domain-match|1|rt=Feb 09 2015 12:33:41 UTC dvc=10.201.78.113 cn3Label=cncPort cn3=53 cn2Label=sid cn2=80494706 shost=dev001srv02.example.com proto=udp cs5Label=cncHost cs5=mfdclk001.org dvchost=DEVFEYE1 spt=51218 dvc=10.100.25.16 smac=00:00:0c:07:ac:00 cn1Label=vlan cn1=0 externalId=851987 cs4Label=link cs4=https://DEVCMS01.example.com/event_stream/events_for_bot?ev_id\\=851987 dmac=00:1d:a2:af:32:a1 cs1Label=sname cs1=Trojan.Generic.DNS",
				"<164>fenotify-3483808.2.alert: 1::~~User-Agent: WinHttpClient::~~Host: www.microads.me::~~Connection: Keep-Alive::~~::~~GET /files/microads/update/InjectScript.js HTTP/1.1::~~User-Agent: WinHttpClient::~~Host: www.microads.me::~~Connection: Keep-Alive::~~::~~GET /files/microads/update/InjectScript.js HTTP/1.1::~~User-Agent: WinHttpClient::~~Host: www.microads.me::~~Connection: Keep-Alive::~~::~~GET /files/microads/update/InjectScript.js HTTP/1.1::~~User-Agent: WinHttpClient::~~Host: www.microads.me::~~Connection: Keep-Alive::~~::~~GET /files/microads/update/InjectScript.js HTTP/1.1::~~User-Agent: WinHttpClient::~~Host: www.microads.me::~~Connection: Keep-Alive::~~::~~GET /files/microads/update/InjectScript.js HTTP/1.1::~~User-Agent: WinHttpClient::~~Host: www.microads.me::~~Connection: Keep-Alive::~~::~~GET /files/microads/update/InjectScript.js HTTP/1.1::~~User-Agent: WinHttpClient::~~Host: www.microads.me::~~Connection: Keep-Alive::~~::~~GET /files/microads/update/InjectScript.js HTTP",
				"<164>fenotify-793972.2.alert: ontrol: no-cache::~~::~~ dmac=00:1d:a2:af:32:a1 cs1Label=sname cs1=Exploit.Kit.Magnitude"
		});
		for (String inputString : getInputStrings())
			assertNotNull(inputString);
		parser = new BasicFireEyeParser();		
	}

	/**
	 * 	
	 * 	
	 * @throws java.lang.Exception
	 */
	public void tearDown() throws Exception {
		parser = null;
	}

	/**
	 * Test method for {@link com.opensoc.parsing.parsers.BasicFireEyeParser#parse(java.lang.String)}.
	 */
	@SuppressWarnings({ "rawtypes", "unused" })
	public void testParse() {
		for (String inputString : getInputStrings()) {
			JSONObject parsed = parser.parse(inputString.getBytes());
			assertNotNull(parsed);
		
			System.out.println(parsed);
			JSONParser parser = new JSONParser();

			Map json=null;
			try {
				json = (Map) parser.parse(parsed.toJSONString());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			Iterator iter = json.entrySet().iterator();
			

			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				String key = (String) entry.getKey();
				String value = (String) json.get("syslog").toString();
				assertNotNull(value);
			}
		}
	}

	/**
	 * Returns Input String
	 */
	public static String[] getInputStrings() {
		return inputStrings;
	}

		
	/**
	 * Sets SourceFire Input String
	 */	
	public static void setInputStrings(String[] strings) {
		BasicFireEyeParserTest.inputStrings = strings;
	}
}