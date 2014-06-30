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

package com.opensoc.enrichments.whois;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.opensoc.enrichment.interfaces.WhoisAdapter;

public class WhoisEnrichmentBolt extends BaseRichBolt {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5032281805777426020L;

	private static final Logger LOG = LoggerFactory
			.getLogger(WhoisEnrichmentBolt.class);

	private String host;
	private String enrichment_tag;
	private Long MAX_CACHE_SIZE;
	private Long MAX_TIME_RETAIN;
	private String enrichment_source_ip;

	private WhoisAdapter adapter;
	private transient CacheLoader<String, String> loader;
	private transient LoadingCache<String, String> cache;
	private OutputCollector _collector;

	public WhoisEnrichmentBolt(WhoisAdapter adapter) {
		this.adapter = adapter;
	}

	@SuppressWarnings("rawtypes")
	public void prepare(Map conf, TopologyContext arg1,
			OutputCollector collector) {

		LOG.info("Initializing WhoisEnrichmentBolt...");
		
		System.out.println("--------------- INITIALIZING WHOIS");

		enrichment_tag = (String) conf.get("whois_enrichment_tag");
		enrichment_source_ip = (String) conf.get("enrichment_source_ip");
		MAX_CACHE_SIZE = (Long) conf.get("MAX_CACHE_SIZE");
		MAX_TIME_RETAIN = (Long) conf.get("MAX_TIME_RETAIN");

		LOG.debug("Setting enrichment_tag to : " + enrichment_tag);
		LOG.debug("Setting enrichment_source_ip to : " + enrichment_source_ip);
		LOG.debug("Setting MAX_CACHE_SIZE to : " + MAX_CACHE_SIZE);
		LOG.debug("Setting MAX_TIME_RETAIN to : " + MAX_TIME_RETAIN);

		_collector = collector;
		
		System.out.println("--------------- INITIALIZING ADAPTER");
		boolean success = adapter.initializeAdapter(enrichment_source_ip);
		

		if (!success)
			LOG.error("Failed to initialize adapter");

		loader = new CacheLoader<String, String>() {
			public String load(String key) throws Exception {
				return adapter.enrich(key);
			}
		};

		cache = CacheBuilder.newBuilder().maximumSize(MAX_CACHE_SIZE)
				.expireAfterWrite(MAX_TIME_RETAIN, TimeUnit.MINUTES)
				.build(loader);

		LOG.info("Whois Enrichment Bolt Initialized...");
	}

	public void execute(Tuple tuple) {

		String original_message = tuple.getString(0).trim();
		LOG.debug("Original message: " + original_message);

		Pattern pattern1 = Pattern.compile(host);
		Matcher matcher = pattern1.matcher(original_message);

		String host_enrichment = "{}";

		try {

			if (matcher.find()) {
				String extracted = matcher.group(1);
				LOG.debug("Extracted term: " + extracted);
				host_enrichment = cache.getUnchecked(extracted);
			}

			_collector.ack(tuple);

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Failed to enrich message: " + original_message);
			_collector.fail(tuple);
		}

		String enriched_message = original_message.substring(0,
				original_message.length() - 1)
				+ ",\""
				+ enrichment_tag
				+ "\":{" + "\"host\":" + host_enrichment + "}}";

		_collector.emit(new Values(enriched_message));
		
		System.out.println("--------------- ENRICHED: " + enriched_message);

	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("message"));
	}

}