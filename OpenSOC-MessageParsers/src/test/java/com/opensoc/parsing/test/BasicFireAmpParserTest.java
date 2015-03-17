/**
 * 
 */
package com.opensoc.parsing.test;



import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.opensoc.parsing.parsers.BasicFireAmpParser;
import com.opensoc.test.AbstractConfigTest;

/**
 * <ul>
 * <li>Title: Test For SourceFireParser</li>
 * <li>Description: </li>
 * <li>Created: July 8, 2014</li>
 * </ul>
 * @version $Revision: 1.0 $
 */
public class BasicFireAmpParserTest extends AbstractConfigTest
{
   /**
    * The inputStrings.
    */
    private static String[] inputStrings;
 
   /**
    * The parser.
    */
    private BasicFireAmpParser parser=null;

	
   /**
    * Constructs a new <code>BasicFireAmpParserTest</code> instance.
    * @throws Exception
    */ 
    public BasicFireAmpParserTest() throws Exception {
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
	}

	/**
	 * @throws java.lang.Exception
	 */
	public void setUp() throws Exception {
        super.setUp("com.opensoc.parsing.test.BasicFireAmpParserTest");
        setInputStrings(super.readTestDataFromFile(this.getConfig().getString("logFile")));
        parser = new BasicFireAmpParser();  
	}

	/**
	 * 	
	 * 	
	 * @throws java.lang.Exception
	 */
	public void tearDown() throws Exception {
		parser = null;
        setInputStrings(null);		
	}

	/**
	 * Test method for {@link com.opensoc.parsing.parsers.BasicFireAmpParser#parse(java.lang.String)}.
	 */
	@SuppressWarnings({ "rawtypes"})
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
				System.out.println("Key:"+key);
				String value = (String) json.get(key).toString();
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
		BasicFireAmpParserTest.inputStrings = strings;
	}
	
    /**
     * Returns the parser.
     * @return the parser.
     */
    public BasicFireAmpParser getParser() {
        return parser;
    }

    /**
     * Sets the parser.
     * @param parser the parser.
     */
     public void setParser(BasicFireAmpParser parser) {
    
        this.parser = parser;
     }
}