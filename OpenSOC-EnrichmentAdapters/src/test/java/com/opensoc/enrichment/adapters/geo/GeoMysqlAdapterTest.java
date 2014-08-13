
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
package com.opensoc.enrichment.adapters.geo;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;

import com.opensoc.test.AbstractTestContext;

 /**
 * <ul>
 * <li>Title: GeoMysqlAdapterTest  </li>
 * <li>Description: JUnit Test for GeoMysqlAdapter </li>
 * <li>Created: Aug 7, 2014 by: spiddapa</li>
 * </ul>
 * @author $Author: spiddapa $
 * @version $Revision: 1.1 $
 */
public class GeoMysqlAdapterTest extends AbstractTestContext {
    /**
     * Class Details if any
     */    
    
     /**
     * The _ip parameter
     */
    private String _ip=null;
    
     /**
     * The _port parameter.
     */
    private String _port=null;
    
     /**
     * The _username parameter.
     */
    private String _username=null;
    
     /**
     * The _password parameter.
     */
    private String _password=null;
    
    /**
    * The _tableName parameter.
    */
   private String _tableName=null;    


   private GeoMysqlAdapter geoMysqlAdapter=null;
    
  

    /**
     * Constructs a new <code>GeoMysqlAdapterTest</code> instance.
     * @param name
     */
    public GeoMysqlAdapterTest(String name) {
        super(name);
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
     * @see com.opensoc.test.AbstractTestContext#setUp()
     */

    protected void setUp() throws Exception {
        super.setUp();
        if(_ip==null){
            _ip=super.getTestProperties().getProperty("bolt.enrichment.geo.adapter.ip");
            System.out.println("ip="+_ip);
        }
        if(_port==null){
            _port=super.getTestProperties().getProperty("bolt.enrichment.geo.adapter.port");
            System.out.println("port="+_port);
        }
        if(_username==null){
            _username=super.getTestProperties().getProperty("bolt.enrichment.geo.adapter.username");
            System.out.println("username="+_username);
        }
        if(_password==null){
            _password=super.getTestProperties().getProperty("bolt.enrichment.geo.adapter.password");
            System.out.println("password="+_password);        
        }
        if(_tableName==null){
            _tableName=super.getTestProperties().getProperty("bolt.enrichment.geo.adapter.table.name");
            System.out.println("tableName="+_tableName);
        }   
       // this.setGeoMysqlAdapter(new GeoMysqlAdapter(this._ip,new Integer(this._port).intValue(),this._username,this._password,this._tableName));      
    }

    /* 
     * (non-Javadoc)
     * @see com.opensoc.test.AbstractTestContext#tearDown()
     */

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test method for {@link com.opensoc.enrichment.adapters.geo.GeoMysqlAdapter#enrich(java.lang.String)}.
     */
    public void testEnrich() {
/*        String geoEnrichmentStr = "{\"sourcefire\":{\"protocol\":\"tcp\",\"ip_dst_addr\":\"72.163.0.129\",\"ip_src_port\":\"45283\",\"ip_dst_port\":\"21\",\"message\":\"SFIMS: [Primary Detection Engine (a7213248-6423-11e3-8537-fac6a92b7d9d)][MTD Access Control] Connection Type: Start, User: Unknown, Client: Unknown, Application Protocol: Unknown, Web App: Unknown, Firewall Rule Name: MTD Access Control, Firewall Rule Action: Allow, Firewall Rule Reasons: Unknown, URL Category: Unknown, URL_Reputation: Risk unknown, URL: Unknown, Interface Ingress: s1p1, Interface Egress: NA, Security Zone Ingress: Unknown, Security Zone Egress: NA, Security Intelligence Matching IP: None, Security Intelligence Category: None, \",\"ip_src_addr\":\"10.5.200.245\",\"key\":\"{TCP} 10.5.200.245:45283 -> 72.163.0.129:21\",\"timestamp\":1405482308648},\"geo_enrichment\":{\"responder_ip_regex_72.163.0.129\":{\"country\":\"US\",\"dmaCode\":\"623\",\"city\":\"Plano\",\"postalCode\":\"\",\"latitude\":\"33.0198\",\"locID\":\"14327\",\"longitude\":\"-96.6989\"},\"originator_ip_regex_10.5.200.245\":{}}}";
        try {
            JSONObject enrichJson=this.getGeoMysqlAdapter().enrich(geoEnrichmentStr);

            Pattern p = Pattern.compile("[^\\._a-z0-9 ]", Pattern.CASE_INSENSITIVE);
            Map<?, ?> json = (Map) enrichJson;
            Iterator<?> iter = json.entrySet().iterator();

            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String) entry.getKey();
                Matcher m = p.matcher(key);
                boolean b = m.find();
                // Test False
                assertFalse(b);
            } 
        }catch(Exception ex) {
                ex.printStackTrace();
                fail("Enrichment failed for :"+geoEnrichmentStr);
            }*/
    }

    /**
     * Test method for {@link com.opensoc.enrichment.adapters.geo.GeoMysqlAdapter#initializeAdapter()}.
     */
    public void testInitializeAdapter() {
//        boolean initialized=this.getGeoMysqlAdapter().initializeAdapter();
//        assertTrue(initialized);
    }

    /**
     * Test method for {@link com.opensoc.enrichment.adapters.geo.GeoMysqlAdapter#GeoMysqlAdapter(java.lang.String, int, java.lang.String, java.lang.String, java.lang.String)}.
     */
    public void testGeoMysqlAdapter() {
        //assertNotNull(this.getGeoMysqlAdapter());

    }
    /**
     * Returns the ip.
     * @return the ip.
     */
    
    public String getIp() {
        return _ip;
    }

    /**
     * Sets the _ip.
     * @param ip the _ip.
     */
    
    public void setIp(String ip) { 
        this._ip = ip;
    }
    
    /**
     * Returns the port.
     * @return the _port.
     */
    
    public String getPort() {
        return _port;
    }

    /**
     * Sets the _port.
     * @param port the port.
     */
    
    public void setPort(String port) {
        this._port = port;
    }    
    
    /**
     * Returns the _username.
     * @return the username.
     */
    
    public String getUsername() {
        return _username;
    }

    /**
     * Sets the _username.
     * @param username the username.
     */
    
    public void setUsername(String username) {
        this._username = username;
    }    
    /**
     * Returns the _password.
     * @return the password.
     */
    
    public String getPassword() {
        return _password;
    }

    /**
     * Sets the _password.
     * @param password the password.
     */ 
    public void setPassword(String password) {
        this._password = password;
    }
    
    /**
     * Returns the _tableName.
     * @return the _tableName.
     */
    public String get_tableName() {
        return _tableName;
    }

    /**
     * Sets the _tableName.
     * @param _tableName the _tableName.
     */
    public void set_tableName(String _tableName) {
    
        this._tableName = _tableName;
    }
    
    /**
     * Returns the geoMysqlAdapter.
     * @return the geoMysqlAdapter.
     */

    public GeoMysqlAdapter getGeoMysqlAdapter() {
        return geoMysqlAdapter;
    }

    /**
     * Sets the geoMysqlAdapter.
     * @param geoMysqlAdapter the geoMysqlAdapter.
     */

    public void setGeoMysqlAdapter(GeoMysqlAdapter geoMysqlAdapter) {

        this.geoMysqlAdapter = geoMysqlAdapter;
    }
}

