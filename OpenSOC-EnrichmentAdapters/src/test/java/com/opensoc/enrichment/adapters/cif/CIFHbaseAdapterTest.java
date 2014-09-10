
 
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
package com.opensoc.enrichment.adapters.cif;

import java.util.Properties;

import com.opensoc.test.AbstractTestContext;


 /**
 * <ul>
 * <li>Title: CIFHbaseAdapterTest</li>
 * <li>Description: Test Class for CIGFHbaseAdapter</li>
 * <li>Created: Aug 7, 2014 by: spiddapa</li>
 * </ul>
 * @author $Author: spiddapa $
 * @version $Revision: 1.1 $
 */
public class CIFHbaseAdapterTest extends AbstractTestContext {

    private static CIFHbaseAdapter cifHbaseAdapter=null;

    /**
    * Any Object for mavenMode
    * @parameter
    *   expression="${mavenMode}"
    *   default-value="Global"
    */
    private Object mavenMode;
    

    /**
     * Constructs a new <code>CIFHbaseAdapterTest</code> instance.
     * @param name
     */

    public CIFHbaseAdapterTest(String name) {
        super(name);
        System.out.println("************** MAVEN MODE="+mavenMode+"  ***********");
    }

    /**
     
     * @throws java.lang.Exception
     */
    protected static void setUpBeforeClass() throws Exception {
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
        Properties prop = super.getTestProperties();
        assertNotNull(prop);
        System.out.println("kafka.zk.list ="+(String) prop.get("kafka.zk.list"));
        System.out.println("kafka.zk.list ="+(String) prop.get("kafka.zk.port"));   
        System.out.println("kafka.zk.list ="+(String) prop.get("bolt.enrichment.cif.tablename"));   
        
        cifHbaseAdapter=new CIFHbaseAdapter((String) prop.get("kafka.zk.list"), (String) prop.get("kafka.zk.port"),(String) prop.get("bolt.enrichment.cif.tablename")); 
    }

    /* 
     * (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */

    protected void tearDown() throws Exception {
        super.tearDown();
        cifHbaseAdapter=null;
    }

    /**
     * Test method for {@link com.opensoc.enrichment.adapters.cif.CIFHbaseAdapter#initializeAdapter()}.
     */
    public void testInitializeAdapter() {
        assertTrue(cifHbaseAdapter.initializeAdapter());
    }

    /**
     * Test method for {@link com.opensoc.enrichment.adapters.cif.CIFHbaseAdapter#enrichByIP(java.lang.String)}.
     */
    public void testEnrichByIP() {
        assertNull(cifHbaseAdapter.enrichByIP("11.1.1"));
    }

    /**
     * Test method for {@link com.opensoc.enrichment.adapters.cif.CIFHbaseAdapter#enrichByDomain(java.lang.String)}.
     */
    public void testEnrichByDomain() {
        assertNull(cifHbaseAdapter.enrichByIP("invaliddomain"));
    }

    /**
     * Test method for {@link com.opensoc.enrichment.adapters.cif.CIFHbaseAdapter#enrichByEmail(java.lang.String)}.
     */
    public void testEnrichByEmail() {
        assertNull(cifHbaseAdapter.enrichByIP("sample@invalid.com"));
    }

    /**
     * Test method for {@link com.opensoc.enrichment.adapters.cif.CIFHbaseAdapter#CIFHbaseAdapter(java.lang.String, java.lang.String, java.lang.String)}.
     */
    public void testCIFHbaseAdapter() {
        assertNotNull(cifHbaseAdapter);
    }

    /**
     * Test method for {@link com.opensoc.enrichment.adapters.cif.CIFHbaseAdapter#enrich(java.lang.String)}.
     */
    public void testEnrich() {
        cifHbaseAdapter.initializeAdapter();
        assertNotNull(cifHbaseAdapter.enrich("testinvalid.metadata"));
        
        assertNotNull(cifHbaseAdapter.enrich("ivalid.ip"));
        assertNotNull(cifHbaseAdapter.enrich("1.1.1.10"));
    }
    

    /**
     * Test method for {@link com.opensoc.enrichment.adapters.cif.CIFHbaseAdapter#getCIFObject(java.lang.String)}.
     */
    public void testGetCIFObject() {
        cifHbaseAdapter.initializeAdapter();
        assertNotNull(cifHbaseAdapter.getCIFObject("testkey"));
    }
    /**
     * Returns the cifHbaseAdapter.
     * @return the cifHbaseAdapter.
     */
    
    public static CIFHbaseAdapter getCifHbaseAdapter() {
        return CIFHbaseAdapterTest.cifHbaseAdapter;
    }

    /**
     * Sets the cifHbaseAdapter.
     * @param cifHbaseAdapter the cifHbaseAdapter.
     */
    
    public static void setCifHbaseAdapter(CIFHbaseAdapter cifHbaseAdapter) {
    
        CIFHbaseAdapterTest.cifHbaseAdapter = cifHbaseAdapter;
    }

    /**
     * Returns the mavenMode.
     * @return the mavenMode.
     */
    
    public Object getMavenMode() {
        return mavenMode;
    }

    /**
     * Sets the mavenMode.
     * @param mavenMode the mavenMode.
     */
    
    public void setMavenMode(Object mavenMode) {
    
        this.mavenMode = mavenMode;
    }    
}

