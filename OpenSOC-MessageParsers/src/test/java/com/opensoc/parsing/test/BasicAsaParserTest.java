package com.opensoc.parsing.test;

import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.opensoc.parsing.parsers.BasicSourcefireParser;
import com.opensoc.parsing.parsers.GrokAsaParser;

import junit.framework.TestCase;

public class BasicAsaParserTest extends TestCase {

	//private  static String grokAsaString = "";
		private static String[] grokAsaStrings;
		private GrokAsaParser grokAsaParser=null;



		/**
		 * @throws java.lang.Exception
		 */
		public static void setUpBeforeClass() throws Exception {
		}

		/**
		 * @throws java.lang.Exception
		 */
		public static void tearDownAfterClass() throws Exception {
			setGrokAsaStrings(null);
		}

		/**
		 * @throws java.lang.Exception
		 */
		public void setUp() throws Exception {
			setGrokAsaStrings(new String[] {
					"<142>Jan  5 08:52:35 10.22.8.201 %ASA-6-302014: Teardown TCP connection 488168292 for DMZ-Inside:10.22.8.51/51231 to Inside-Trunk:10.22.8.174/40004 duration 0:00:00 bytes 2103 TCP FINs",
					"<142>Jan  5 08:52:35 10.22.8.201 %ASA-6-106015: Deny TCP (no connection) from 186.111.72.11/80 to 204.111.72.226/45019 flags SYN ACK  on interface Outside_VPN",
					"<166>Jan  5 09:52:35 10.22.8.12 %ASA-6-302014: Teardown TCP connection 17604987 for outside:209.111.72.151/443 to inside:10.22.8.188/64306 duration 0:00:31 bytes 10128 TCP FINs",
					"<166>Jan  5 09:52:35 10.22.8.12 %ASA-6-302014: Teardown TCP connection 17604999 for outside:209.111.72.151/443 to inside:10.22.8.188/64307 duration 0:00:30 bytes 6370 TCP FINs",
					"<142>Jan  5 08:52:35 10.22.8.201 %ASA-6-302014: Teardown TCP connection 488167347 for Outside_VPN:198.111.72.24/2134 to DMZ-Inside:10.22.8.53/443 duration 0:00:01 bytes 9785 TCP FINs",
					"<174>Jan  5 14:52:35 10.22.8.212 %ASA-6-302015: Built inbound UDP connection 76245506 for outside:10.22.8.110/49886 (10.22.8.110/49886) to inside:192.111.72.8/8612 (192.111.72.8/8612) (user.name)",
					"<166>Jan  5 08:52:35 10.22.8.216 %ASA-6-302014: Teardown TCP connection 212805993 for outside:10.22.8.89/56917(LOCAL\\user.name) to inside:216.111.72.126/443 duration 0:00:00 bytes 0 TCP FINs (user.name)",
					"<167>Jan  5 08:52:35 10.22.8.216 %ASA-7-710005: UDP request discarded from 10.22.8.223/49192 to outside:224.111.72.252/5355"

			});
			for (String grokAsaString : getGrokAsaStrings())
				assertNotNull(grokAsaString);
			grokAsaParser = new GrokAsaParser();		
		}

		/**
		 * 	
		 * 	
		 * @throws java.lang.Exception
		 */
		public void tearDown() throws Exception {
			grokAsaParser = null;
		}

		/**
		 * Test method for {@link com.opensoc.parsing.parsers.BasicSourcefireParser#parse(java.lang.String)}.
		 */
		@SuppressWarnings({ "rawtypes", "unused" })
		public void testParse() {
			for (String grokAsaString : getGrokAsaStrings()) {
				JSONObject parsed = grokAsaParser.parse(grokAsaString.getBytes());
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
					//String value = (String) json.get("original_string").toString();
					//assertNotNull(value);
				}
			}
		}

		/**
		 * Returns GrokAsa Input String
		 */
		public static String[] getGrokAsaStrings() {
			return grokAsaStrings;
		}

			
		/**
		 * Sets GrokAsa Input String
		 */	
		public static void setGrokAsaStrings(String[] strings) {
			BasicAsaParserTest.grokAsaStrings = strings;
		}
	}