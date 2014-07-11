/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.opensoc.enrichment.common;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.base.BaseRichBolt;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@SuppressWarnings("rawtypes")
public abstract class AbstractEnrichmentBolt extends BaseRichBolt {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6710596708304282838L;

	protected static final Logger LOG = LoggerFactory
			.getLogger(AbstractEnrichmentBolt.class);

	protected OutputCollector _collector;
	protected String _OutputFieldName;
	
	protected String _enrichment_tag;
	protected Long _MAX_CACHE_SIZE;
	protected Long _MAX_TIME_RETAIN;
	protected String _enrichment_source_ip;
	
	protected Map<String, Pattern> _patterns;
	protected EnrichmentAdapter _adapter;
	
	protected transient CacheLoader<String, JSONObject> loader;
	protected transient LoadingCache<String, JSONObject> cache;


	public final void prepare(Map conf, TopologyContext topologyContext,
			OutputCollector collector) {
		_collector = collector;

		if (this._OutputFieldName == null)
			throw new IllegalStateException("OutputFieldName must be specified");
		if (this._enrichment_tag == null)
			throw new IllegalStateException("enrichment_tag must be specified");
		if (this._MAX_CACHE_SIZE == null)
			throw new IllegalStateException("MAX_CACHE_SIZE must be specified");
		if (this._MAX_TIME_RETAIN == null)
			throw new IllegalStateException("MAX_TIME_RETAIN must be specified");
		if (this._adapter == null)
			throw new IllegalStateException("Adapter must be specified");
		if(this._patterns == null)
			throw new IllegalStateException("Patterns must be specified");
		
		loader = new CacheLoader<String, JSONObject>() {
			public JSONObject load(String key) throws Exception {
				return _adapter.enrich(key);
			}
		};

		cache = CacheBuilder.newBuilder().maximumSize(_MAX_CACHE_SIZE)
				.expireAfterWrite(_MAX_TIME_RETAIN, TimeUnit.MINUTES)
				.build(loader);

		boolean success = _adapter.initializeAdapter();

		if (!success) {
			LOG.error("EnrichmentBolt could not initialize adapter");
			throw new IllegalStateException("Could not initialize adapter...");
		}

		LOG.info("EnrichmentBolt Initialized...");

		try {
			doPrepare(conf, topologyContext, collector);
		} catch (IOException e) {
			LOG.error("Counld not initialize...");
			e.printStackTrace();
		}
		
	}

	abstract void doPrepare(Map conf, TopologyContext topologyContext,
			OutputCollector collector) throws IOException;

}