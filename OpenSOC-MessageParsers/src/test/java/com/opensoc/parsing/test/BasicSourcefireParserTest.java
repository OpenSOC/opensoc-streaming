/**
 * 
 */
package com.opensoc.parsing.test;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;

import org.json.simple.JSONObject;

import com.opensoc.parsing.parsers.BasicSourcefireParser;
import com.opensoc.test.AbstractSchemaTest;

/**
 * <ul>
 * <li>Title: Test For SourceFireParser</li>
 * <li>Description: </li>
 * <li>Created: July 8, 2014</li>
 * </ul>
 * @version $Revision: 1.0 $
 */
public class BasicSourcefireParserTest extends AbstractSchemaTest
{
	
	 /**
	 * The sourceFireString.
	 */	 
	private  static String sourceFireString = "";
	
	 /**
	 * The sourceFireParser.
	 */
	 
	private static BasicSourcefireParser sourceFireParser=null; 


    /**
	 * @throws java.lang.Exception
	 */
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	public static void tearDownAfterClass() throws Exception {
		setSourceFireString("");
	}

	/**
	 * @throws java.lang.Exception
	 */
	public void setUp() throws Exception {
<<<<<<< HEAD
		setSourceFireString("SFIMS: [Primary Detection Engine (a7213248-6423-11e3-8537-fac6a92b7d9d)][MTD Access Control] Connection Type: Start, User: Unknown, Client: Unknown, Application Protocol: Unknown, Web App: Unknown, Firewall Rule Name: MTD Access Control, Firewall Rule Action: Allow, Firewall Rule Reasons: Unknown, URL Category: Unknown, URL_Reputation: Risk unknown, URL: Unknown, Interface Ingress: s1p1, Interface Egress: N/A, Security Zone Ingress: Unknown, Security Zone Egress: N/A, Security Intelligence Matching IP: None, Security Intelligence Category: None, {TCP} 72.163.0.129:60517 -> 10.1.128.236:443");		assertNotNull(getSourceFireString());
		sourceFireParser = new BasicSourcefireParser();		
=======
		setSourceFireString("SFIMS: [Primary Detection Engine (a7213248-6423-11e3-8537-fac6a92b7d9d)][MTD Access Control] Connection Type: Start, User: Unknown, Client: Unknown, Application Protocol: Unknown, Web App: Unknown, Firewall Rule Name: MTD Access Control, Firewall Rule Action: Allow, Firewall Rule Reasons: Unknown, URL Category: Unknown, URL_Reputation: Risk unknown, URL: Unknown, Interface Ingress: s1p1, Interface Egress: N/A, Security Zone Ingress: Unknown, Security Zone Egress: N/A, Security Intelligence Matching IP: None, Security Intelligence Category: None, {TCP} 72.163.0.129:60517 -> 10.1.128.236:443");		
		assertNotNull(getSourceFireString());
		BasicSourcefireParserTest.setSourceFireParser(new BasicSourcefireParser());	
        URL schema_url = getClass().getClassLoader().getResource(
            "TestSchemas/SourcefireSchema.json");
        super.setSchemaJsonString(super.readSchemaFromFile(schema_url));    		
>>>>>>> FETCH_HEAD
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
	 * @throws Exception 
	 */
<<<<<<< HEAD
	@SuppressWarnings({ "rawtypes", "unused" })
	public void testParse() {
		JSONObject parsed = sourceFireParser.parse(getSourceFireString().getBytes());
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
=======
	public void testParse() throws Exception {
        URL log_url = getClass().getClassLoader().getResource("SourceFireSample.log");

        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(log_url.getFile()));
            String line = "";
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                assertNotNull(line);
                JSONObject parsed =  sourceFireParser.parse(line.getBytes());
                System.out.println(parsed);
                assertEquals(true, validateJsonData(super.getSchemaJsonString(), parsed.toString()));
            }
            br.close();  
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            
        }	    
>>>>>>> FETCH_HEAD
	}

	/**
	 * Returns SourceFire Input String
	 */
	public static String getSourceFireString() {
		return sourceFireString;
	}

		
	/**
	 * Sets SourceFire Input String
	 */	
	public static void setSourceFireString(String sourceFireString) {
		BasicSourcefireParserTest.sourceFireString = sourceFireString;
	}
	
    /**
     * Returns the sourceFireParser.
     * @return the sourceFireParser.
     */
    
    public BasicSourcefireParser getSourceFireParser() {
        return BasicSourcefireParserTest.sourceFireParser;
    }

    /**
     * Sets the sourceFireParser.
     * @param sourceFireParser the sourceFireParser.
     */
    
    public static void setSourceFireParser(BasicSourcefireParser sourceFireParser) {
        BasicSourcefireParserTest.sourceFireParser = sourceFireParser;
    }	
}