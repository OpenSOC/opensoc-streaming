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

import com.opensoc.parsing.parsers.BasicSourcefireParser;

/**
 * <ul>
 * <li>Title: Test For SourceFireParser</li>
 * <li>Description: </li>
 * <li>Created: July 8, 2014</li>
 * </ul>
 * @version $Revision: 1.0 $
 */
public class BasicSourcefireParserTest extends TestCase
	{

	//private  static String sourceFireString = "";
	private static String[] sourceFireStrings;
	private BasicSourcefireParser sourceFireParser=null;



	/**
	 * @throws java.lang.Exception
	 */
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	public static void tearDownAfterClass() throws Exception {
		setSourceFireStrings(null);
	}

	/**
	 * @throws java.lang.Exception
	 */
	public void setUp() throws Exception {
		setSourceFireStrings(new String[] {
				"SFIMS: [Primary Detection Engine (a7213248-6423-11e3-8537-fac6a92b7d9d)][MTD Access Control] Connection Type: Start, User: Unknown, Client: Unknown, Application Protocol: Unknown, Web App: Unknown, Firewall Rule Name: MTD Access Control, Firewall Rule Action: Allow, Firewall Rule Reasons: Unknown, URL Category: Unknown, URL_Reputation: Risk unknown, URL: Unknown, Interface Ingress: s1p1, Interface Egress: N/A, Security Zone Ingress: Unknown, Security Zone Egress: N/A, Security Intelligence Matching IP: None, Security Intelligence Category: None, {TCP} 72.163.0.129:60517 -> 10.1.128.236:443",
				"snort: [1:3192:2] WEB-CLIENT Windows Media Player directory traversal via Content-Disposition attempt [Classification: Attempted User Privilege Gain] [Priority: 1] {TCP} 46.149.110.103:80 -> 192.168.56.102:1073",
				"SFIMS: Correlation Event: Open Soc Log Forwarding/Opensoc Log Forwarding at Thu Oct 23 04:55:39 2014 UTC: [1:19123:7] \"MALWARE-CNC Dropper Win.Trojan.Cefyns.A variant outbound connection\" [Impact: Unknown] From \"172.19.50.7\" at Thu Oct 23 04:55:38 2014 UTC [Classification: A Network Trojan was Detected] [Priority: 1] {tcp} 139.230.245.23:52078->72.52.4.91:80"
		});
		for (String sourceFireString : getSourceFireStrings())
			assertNotNull(sourceFireString);
		sourceFireParser = new BasicSourcefireParser();		
	}

	/**
	 * 	
	 * 	
	 * @throws java.lang.Exception
	 */
	public void tearDown() throws Exception {
		sourceFireParser = null;
	}

	/**
	 * Test method for {@link com.opensoc.parsing.parsers.BasicSourcefireParser#parse(java.lang.String)}.
	 */
	@SuppressWarnings({ "rawtypes", "unused" })
	public void testParse() {
		for (String sourceFireString : getSourceFireStrings()) {
			JSONObject parsed = sourceFireParser.parse(sourceFireString.getBytes());
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
				String value = (String) json.get("original_string").toString();
				assertNotNull(value);
			}
		}
	}

	/**
	 * Returns SourceFire Input String
	 */
	public static String[] getSourceFireStrings() {
		return sourceFireStrings;
	}

		
	/**
	 * Sets SourceFire Input String
	 */	
	public static void setSourceFireStrings(String[] strings) {
		BasicSourcefireParserTest.sourceFireStrings = strings;
	}
}