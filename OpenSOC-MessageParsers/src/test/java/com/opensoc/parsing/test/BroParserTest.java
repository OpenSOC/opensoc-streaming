package com.opensoc.parsing.test;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import org.json.simple.JSONObject;

import com.opensoc.parsing.parsers.BasicBroParser;
import com.opensoc.test.AbstractSchemaTest;

/**
 * <ul>
 * <li>Title: Test For BroParser</li>
 * <li>Description: </li>
 * <li>Created: July 8, 2014</li>
 * </ul>
 * @version $Revision: 1.0 $
 */
public class BroParserTest extends AbstractSchemaTest {
	
	
	 /**
	 * The broJsonString.
	 */
	 
	private static String broJsonString="";
	
	 /**
	 * The broParser.
	 */
	 
	private static BasicBroParser broParser=null;
	
    /**
     * Constructs a new <code>BroParserTest</code> instance.
     */
    public BroParserTest() {
        super();
    }	


	/**
	 * @throws java.lang.Exception
	 */
	public static void setUpBeforeClass() throws Exception {
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	public static void tearDownAfterClass() throws Exception {
		setBroJsonString("");
	}

	/**
	 * @throws java.lang.Exception
	 */
	public void setUp() throws Exception {
	    setBroJsonString("{\"http\":{\"ts\":1402307733473,\"uid\":\"CTo78A11g7CYbbOHvj\",\"id.orig_h\":\"192.249.113.37\",\"id.orig_p\":58808,\"id.resp_h\":\"72.163.4.161\",\"id.resp_p\":80,\"trans_depth\":1,\"method\":\"GET\",\"host\":\"www.cisco.com\",\"uri\":\"/\",\"user_agent\":\"curl/7.22.0 (x86_64-pc-linux-gnu) libcurl/7.22.0 OpenSSL/1.0.1 zlib/1.2.3.4 libidn/1.23 librtmp/2.3\",\"request_body_len\":0,\"response_body_len\":25523,\"status_code\":200,\"status_msg\":\"OK\",\"tags\":[],\"resp_fuids\":[\"FJDyMC15lxUn5ngPfd\"],\"resp_mime_types\":[\"text/html\"]}}");	    
		assertNotNull(getBroJsonString());
		BroParserTest.setBroParser(new BasicBroParser());		
        URL schema_url = getClass().getClassLoader().getResource(
            "TestSchemas/BroSchema.json");
        super.setSchemaJsonString(super.readSchemaFromFile(schema_url)); 		
	}
	
	/**
<<<<<<< HEAD
	 * @throws ParseException
	 * Tests for Parse Method
	 * Parses Static json String and checks if any spl chars are present in parsed string.
	 */
	@SuppressWarnings({ "unused", "rawtypes" })
	public void testParse() throws ParseException {


		BasicBroParser broparser = new BasicBroParser();
		assertNotNull(getBroJsonString());
		JSONObject cleanJson = broparser.parse(getBroJsonString().getBytes());
        assertNotNull(cleanJson);		
		System.out.println(cleanJson);


		Pattern p = Pattern.compile("[^\\._a-z0-9 ]", Pattern.CASE_INSENSITIVE);

		JSONParser parser = new JSONParser();

		Map json = (Map) cleanJson;
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

=======
	 * @throws Exception 
	 * @throws IOException 
	 */
	public void testParse() throws IOException, Exception {
        URL log_url = getClass().getClassLoader().getResource("BroSample.log");

        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(log_url.getFile()));
            String line = "";
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                assertNotNull(line);
                JSONObject parsed =  broParser.parse(line.getBytes());
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
     * Returns the instance of BroParser
     */
	public static BasicBroParser getBroParser() {
		return broParser;
	}
    /**
     * Sets the instance of BroParser
     */
	public static void setBroParser(BasicBroParser broParser) {
		BroParserTest.broParser = broParser;
	}
    /**
     * Return BroPaser JSON String
     */
	public static String getBroJsonString() {
		return BroParserTest.broJsonString;
	}

    /**
     * Sets BroPaser JSON String
     */
	public static void setBroJsonString(String broJsonString) {
		BroParserTest.broJsonString = broJsonString;
	}	
}
