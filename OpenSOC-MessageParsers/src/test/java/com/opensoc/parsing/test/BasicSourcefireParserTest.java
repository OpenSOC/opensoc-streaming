/**
 * 
 */
package com.opensoc.parsing.test;



import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.opensoc.parsing.parsers.BasicSourcefireParser;
import com.opensoc.test.AbstractConfigTest;

/**
 * <ul>
 * <li>Title: Test For SourceFireParser</li>
 * <li>Description: </li>
 * <li>Created: July 8, 2014</li>
 * </ul>
 * @version $Revision: 1.0 $
 */
public class BasicSourcefireParserTest extends AbstractConfigTest
{
     /**
     * The sourceFireStrings.
     */    
    private static String[] sourceFireStrings;
    
     /**
     * The sourceFireParser.
     */
    private BasicSourcefireParser sourceFireParser=null;


    /**
     * Constructs a new <code>BasicSourcefireParserTest</code> instance.
     * @throws Exception
     */
     
    public BasicSourcefireParserTest() throws Exception {
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
		setSourceFireStrings(null);
	}

	/**
	 * @throws java.lang.Exception
	 */
	public void setUp() throws Exception {
        super.setUp("com.opensoc.parsing.test.BasicSoureceFireParserTest");
        setSourceFireStrings(super.readTestDataFromFile(this.getConfig().getString("logFile")));
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
		    byte[] srcBytes = sourceFireString.getBytes();
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
    /**
    * Returns the sourceFireParser.
    * @return the sourceFireParser.
    */
   
   public BasicSourcefireParser getSourceFireParser() {
       return sourceFireParser;
   }

   /**
    * Sets the sourceFireParser.
    * @param sourceFireParser the sourceFireParser.
    */
   
   public void setSourceFireParser(BasicSourcefireParser sourceFireParser) {
   
       this.sourceFireParser = sourceFireParser;
   }	
}