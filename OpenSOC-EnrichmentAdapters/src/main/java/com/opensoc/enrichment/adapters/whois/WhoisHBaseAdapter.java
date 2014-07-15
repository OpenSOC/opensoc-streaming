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

package com.opensoc.enrichment.adapters.whois;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.json.simple.JSONObject;

public class WhoisHBaseAdapter extends AbstractWhoisAdapter {
	
	private HTableInterface table;
	private String _table_name;
	
	public WhoisHBaseAdapter(String table_name)
	{
		_table_name=table_name;
	}
	
	public boolean initializeAdapter() {
		Configuration conf = null;
		conf = HBaseConfiguration.create();

		try {
			
			LOG.debug("=======Connecting to HBASE===========");
			LOG.debug("=======ZOOKEEPER = "
					+ conf.get("hbase.zookeeper.quorum"));
			
			HConnection connection = HConnectionManager.createConnection(conf);
			table = connection.getTable(_table_name);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;

	}

	public JSONObject enrich(String metadata) {
		LOG.debug("=======Pinging HBase For:" + metadata);
		
		JSONObject jo = new JSONObject();

		Get get = new Get(metadata.getBytes());
		Result rs;

		try {
			rs = table.get(get);

			for (KeyValue kv : rs.raw())
				jo.put(metadata, new String(kv.getValue()));

		} catch (IOException e) {
			jo.put(metadata, "{}");
			e.printStackTrace();
		}
		
		return jo;

	}

}
