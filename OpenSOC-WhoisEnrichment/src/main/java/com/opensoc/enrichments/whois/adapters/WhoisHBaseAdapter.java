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

package com.opensoc.enrichments.whois.adapters;

import java.io.Serializable;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;

import com.opensoc.enrichment.interfaces.WhoisAdapter;

public class WhoisHBaseAdapter implements WhoisAdapter, Serializable {

	private static final long serialVersionUID = 2675621052400811942L;
	
	public Logger _LOG;

	public String enrich(String metadata) {
		// TODO Auto-generated method stub
		return "{}";
	}

	public boolean initializeAdapter(String ip, Logger LOG) {
		
		_LOG = LOG;
		
		try {
			Configuration config = HBaseConfiguration.create();
			config.set("hbase.master", ip);
			config.set("hbase.zookeeper.quorum","172.30.9.95");
			config.set("hbase.zookeeper.property.clientPort", "2181");
			
			HTable table = new HTable(config, "whois");
			Get g = new Get(Bytes.toBytes("002391.com"));
			Result r = table.get(g);
			byte [] value = r.getValue(Bytes.toBytes("data:json"),Bytes.toBytes("values"));
			String valueStr = Bytes.toString(value);
		    System.out.println("---------------------------GET: " + valueStr);
		    
		    return true;
			
		} catch (Exception e) {
			
			System.out.println("--------------- FAILED TO INITIALIZE");
			e.printStackTrace();
			return false;
		}

		
	}

}
