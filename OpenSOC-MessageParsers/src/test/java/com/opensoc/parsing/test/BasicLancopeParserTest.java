  /*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.opensoc.parsing.test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import org.json.simple.JSONObject;

import com.opensoc.parsing.parsers.BasicLancopeParser;
import com.opensoc.test.AbstractSchemaTest;

 /**
 * <ul>
 * <li>Title: Junit for LancopeParserTest</li>
 * <li>Description: </li>
 * <li>Created: Aug 25, 2014</li>
 * </ul>
 * @version $Revision: 1.1 $
 */
public class BasicLancopeParserTest extends AbstractSchemaTest {

    private  static String rawMessage = "";
    private static BasicLancopeParser lancopeParser=null;   

    /**
     * Constructs a new <code>BasicLancopeParserTest</code> instance.
     * @param name
     */

    public BasicLancopeParserTest(String name) {
        super(name);
    }

    /**
     
     * @throws java.lang.Exception
     */
    protected static void setUpBeforeClass() throws Exception {
        setRawMessage("{\"message\":\"<131>Jul 17 15:59:01 smc-01 StealthWatch[12365]: 2014-07-17T15:58:30Z 10.40.10.254 0.0.0.0 Minor High Concern Index The host's concern index has either exceeded the CI threshold or rapidly increased. Observed 36.55M points. Policy maximum allows up to 20M points.\",\"@version\":\"1\",\"@timestamp\":\"2014-07-17T15:56:05.992Z\",\"type\":\"syslog\",\"host\":\"10.122.196.201\"}");        
    }

    /**
     
     * @throws java.lang.Exception
     */
    protected static void tearDownAfterClass() throws Exception {
    }

    /* 
     * (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */

    protected void setUp() throws Exception {
        super.setUp();
        setRawMessage("{\"message\":\"<131>Jul 17 15:59:01 smc-01 StealthWatch[12365]: 2014-07-17T15:58:30Z 10.40.10.254 0.0.0.0 Minor High Concern Index The host's concern index has either exceeded the CI threshold or rapidly increased. Observed 36.55M points. Policy maximum allows up to 20M points.\",\"@version\":\"1\",\"@timestamp\":\"2014-07-17T15:56:05.992Z\",\"type\":\"syslog\",\"host\":\"10.122.196.201\"}");        
        assertNotNull(getRawMessage());
        BasicLancopeParserTest.setLancopeParser(new BasicLancopeParser());   
        
        URL schema_url = getClass().getClassLoader().getResource(
            "TestSchemas/LancopeSchema.json");
        super.setSchemaJsonString(super.readSchemaFromFile(schema_url));      
    }

    /* 
     * (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test method for {@link com.opensoc.parsing.parsers.BasicLancopeParser#parse(byte[])}.
     * @throws Exception 
     * @throws IOException 
     */
    public void testParse() throws IOException, Exception {
        URL log_url = getClass().getClassLoader().getResource("LancopeSample.log");

        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(log_url.getFile()));
            String line = "";
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                assertNotNull(line);
                JSONObject parsed =  lancopeParser.parse(line.getBytes());
                System.out.println(parsed);
                assertEquals(true, validateJsonData(super.getSchemaJsonString(), parsed.toString()));
            }
            br.close();  
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            
        }
    }
    
    /**
     * Returns the rawMessage.
     * @return the rawMessage.
     */
    
    public static String getRawMessage() {
        return BasicLancopeParserTest.rawMessage;
    }

    /**
     * Sets the rawMessage.
     * @param rawMessage the rawMessage.
     */
    
    public static void setRawMessage(String rawMessage) {
    
        BasicLancopeParserTest.rawMessage = rawMessage;
    }
    /**
     * Returns the lancopeParser.
     * @return the lancopeParser.
     */
    
    public static BasicLancopeParser getLancopeParser() {
        return lancopeParser;
    }

    /**
     * Sets the lancopeParser.
     * @param lancopeParser the lancopeParser.
     */
    
    public static void setLancopeParser(BasicLancopeParser lancopeParser) {
    
        BasicLancopeParserTest.lancopeParser = lancopeParser;
    }
}

