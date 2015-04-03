package com.opensoc.parsing.test;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.opensoc.parsing.parsers.BasicBroParser;
import com.opensoc.test.AbstractConfigTest;

/**
 * <ul>
 * <li>Title: Test For BroParser</li>
 * <li>Description: </li>
 * <li>Created: July 8, 2014</li>
 * </ul>
 * @version $Revision: 1.0 $
 */

 /**
 * <ul>
 * <li>Title: </li>
 * <li>Description: </li>
 * <li>Created: Feb 20, 2015 </li>
 * </ul>
 * @author $Author: $
 * @version $Revision: 1.1 $
 */
public class BroParserTest extends AbstractConfigTest {
	
	
	 /**
	 * The broJsonString.
	 */
	private static String broJsonString="";

     /**
     * The parser.
     */
    private static BasicBroParser parser=null;
	
    /**
     * Constructs a new <code>BroParserTest</code> instance.
     * @throws Exception 
     */
    public BroParserTest() throws Exception {
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
        super.setUp("com.opensoc.parsing.test.BroParserTest");
        setBroJsonString(super.readTestDataFromFile(this.getConfig().getString("logFile"))[0]);
        parser = new BasicBroParser();  
	}
	
	/**
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
    /**
     * Returns the parser.
     * @return the parser.
     */
    
    public static BasicBroParser getParser() {
        return parser;
    }


    /**
     * Sets the parser.
     * @param parser the parser.
     */
    
    public static void setParser(BasicBroParser parser) {
    
        BroParserTest.parser = parser;
    }	
}
