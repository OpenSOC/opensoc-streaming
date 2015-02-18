package com.opensoc.parsing.test;

import java.util.Iterator;
import java.util.Map;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.opensoc.parsing.parsers.BasicPaloAltoFirewallParser;

import junit.framework.TestCase;

public class BasicPaloAltoFirewallParserTest extends TestCase {

		private static String[] inputStrings;
		private BasicPaloAltoFirewallParser paParser=null;



		/**
		 * @throws java.lang.Exception
		 */
		public static void setUpBeforeClass() throws Exception {
		}

		/**
		 * @throws java.lang.Exception
		 */
		public static void tearDownAfterClass() throws Exception {
			setPAStrings(null);
		}

		/**
		 * @throws java.lang.Exception
		 */
		public void setUp() throws Exception {
			setPAStrings(new String[] {
					"<11>Jan  5 05:38:59 PAN1.exampleCustomer.com 1,2015/01/05 05:38:58,0006C110285,THREAT,vulnerability,1,2015/01/05 05:38:58,10.0.0.115,216.0.10.198,0.0.0.0,0.0.0.0,EX-Allow,example\\user.name,,web-browsing,vsys1,internal,external,ethernet1/2,ethernet1/1,LOG-Default,2015/01/05 05:38:58,12031,1,54180,80,0,0,0x80004000,tcp,reset-both,\"ad.aspx?f=300x250&id=12;tile=1;ord=67AF705D60B1119C0F18BEA336F9\",HTTP: IIS Denial Of Service Attempt(40019),any,high,client-to-server,347368099,0x0,10.0.0.0-10.255.255.255,US,0,,1200568889751109656,,",
					"<14>Jan  5 12:51:34 PAN1.exampleCustomer.com 1,2015/01/05 12:51:33,0011C103117,TRAFFIC,end,1,2015/01/05 12:51:33,10.0.0.39,10.1.0.163,0.0.0.0,0.0.0.0,EX-Allow,,example\\user.name,ms-ds-smb,vsys1,v_external,v_internal,ethernet1/2,ethernet1/1,LOG-Default,2015/01/05 12:51:33,33760927,1,52688,445,0,0,0x401a,tcp,allow,2229,1287,942,10,2015/01/05 12:51:01,30,any,0,17754932062,0x0,10.0.0.0-10.255.255.255,10.0.0.0-10.255.255.255,0,6,4"
					
					
			});
			for (String paString : getInputStrings())
				assertNotNull(paString);
			paParser = new BasicPaloAltoFirewallParser();		
		}

		/**
		 * 	
		 * 	
		 * @throws java.lang.Exception
		 */
		public void tearDown() throws Exception {
			paParser = null;
		}

		/**
		 * Test method for {@link com.opensoc.parsing.parsers.BasicSourcefireParser#parse(java.lang.String)}.
		 */
		@SuppressWarnings({ "rawtypes", "unused" })
		public void testParse() {
			for (String inputString : getInputStrings()) {
				JSONObject parsed = paParser.parse(inputString.getBytes());
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
		 * Returns  Input String
		 */
		public static String[] getInputStrings() {
			return inputStrings;
		}

			
		/**
		 * Sets  Input String
		 */	
		public static void setPAStrings(String[] strings) {
			BasicPaloAltoFirewallParserTest.inputStrings = strings;
		}
	}