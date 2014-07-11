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
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.KeyValue;

public class CIFHbaseAdapter extends AbstractCIFAdapter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final int MAX_CACHE_SIZE = 10000;
	private final int MAX_TIME_RETAIN = 10;
	private String tableName = "cif_infrastructure";

	CacheLoader<String, Map> loader = new CacheLoader<String, Map>() {
		public Map load(String key) throws Exception {
			return getCIFObject(key);
		}
	};

	LoadingCache<String, Map> cache = CacheBuilder.newBuilder()
			.maximumSize(MAX_CACHE_SIZE)
			.expireAfterWrite(MAX_TIME_RETAIN, TimeUnit.MINUTES).build(loader);

	private HTableInterface table;

	public JSONObject enrich(String metadata) {
		// TODO Auto-generated method stub
		return null;
	}

	protected Map getCIFObject(String key) {
		// TODO Auto-generated method stub
		Get get = new Get(key.getBytes());
		Result rs;
		Map output = new HashMap();

		try {
			rs = table.get(get);
			
			for (KeyValue kv : rs.raw()) 
				output.put(new String(kv.getFamily()), "Y");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}

	private Map getEnhancement(String IP) {
		return cache.getUnchecked(IP);
	}

	@Override
	public boolean initializeAdapter() {
		// TODO Auto-generated method stub

		Configuration conf = null;
		conf = HBaseConfiguration.create();

		try {
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
