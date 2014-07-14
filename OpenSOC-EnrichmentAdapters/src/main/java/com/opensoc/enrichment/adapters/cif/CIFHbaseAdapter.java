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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.log4j.Logger;

@SuppressWarnings("unchecked")
public class CIFHbaseAdapter extends AbstractCIFAdapter {

	private static final long serialVersionUID = 1L;
	private String tableName = "cif_table";
	private HTableInterface table;

	/** The LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(CIFHbaseAdapter.class);

	public JSONObject enrich(String metadata) {
		// TODO Auto-generated method stub

		JSONObject output = new JSONObject();
		LOGGER.debug("=======Looking Up For:" + metadata);
		output.putAll(getCIFObject(metadata));

		return output;
	}

	protected Map getCIFObject(String key) {
		// TODO Auto-generated method stub

		LOGGER.debug("=======Pinging HBase For:" + key);

		Get get = new Get(key.getBytes());
		Result rs;
		Map output = new HashMap();

		try {
			rs = table.get(get);

			for (KeyValue kv : rs.raw())
				output.put(new String(kv.getQualifier()), "Y");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}

	@Override
	public boolean initializeAdapter() {
		// TODO Auto-generated method stub

		// Initialize HBase Table
		Configuration conf = null;
		conf = HBaseConfiguration.create();

		try {
			LOGGER.debug("=======Connecting to HBASE===========");
			LOGGER.debug("=======ZOOKEEPER = "
					+ conf.get("hbase.zookeeper.quorum"));
			HConnection connection = HConnectionManager.createConnection(conf);
			table = connection.getTable(tableName);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public String enrichByIP(String metadata) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String enrichByDomain(String metadata) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String enrichByEmail(String metadata) {
		// TODO Auto-generated method stub
		return null;
	}

}
