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

package com.opensoc.enrichments.geo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.opensoc.enrichment.interfaces.GeoAdapter;


@SuppressWarnings("serial")
public class GeoEnrichmentBolt extends BaseRichBolt {

	private static final Logger LOG = LoggerFactory.getLogger(GeoEnrichmentBolt.class);
	
	private String originator_ip;
	private String responder_ip;
	private String enrichment_tag;
	private Long MAX_CACHE_SIZE;
	private Long MAX_TIME_RETAIN;
	private String enrichment_source_ip;
	
	private GeoAdapter adapter;
	private transient CacheLoader<String, String> loader;
	private transient LoadingCache<String, String> cache;
	private OutputCollector _collector;

	public GeoEnrichmentBolt(GeoAdapter adapter) {
		this.adapter = adapter;
	}

	@SuppressWarnings("rawtypes")
	public void prepare(Map conf, TopologyContext arg1, OutputCollector collector) {

		LOG.info("Preparing GeoEnrichmentBolt Bolt...");
		
		originator_ip = (String) conf.get("originator_ip_regex");
		responder_ip = (String) conf.get("responder_ip_regex");
		enrichment_tag = (String) conf.get("geo_enrichment_tag");
		MAX_CACHE_SIZE = (Long) conf.get("MAX_CACHE_SIZE");
		MAX_TIME_RETAIN= (Long) conf.get("MAX_TIME_RETAIN");
		enrichment_source_ip = (String) conf.get("geo_enrichment_source_ip");
		
		LOG.debug("Setting originator_ip: " + originator_ip);
		LOG.debug("Setting responder_ip: " + responder_ip);
		LOG.debug("Setting enrichment_tag: " + enrichment_tag);
		LOG.debug("Setting MAX_CACHE_SIZE: " + MAX_CACHE_SIZE);
		LOG.debug("Setting MAX_TIME_RETAIN: " + MAX_TIME_RETAIN);
		LOG.debug("Setting enrichment_source_ip: " + enrichment_source_ip);
		
		_collector = collector;
		boolean success = adapter.initializeAdapter(enrichment_source_ip, LOG);
		
		if(!success)
		{
			LOG.error("GeoEnrichmentBolt could not initialize adapter");
		}
		
		LOG.info("GeoEnrichmentBolt Initialized...");

		loader = new CacheLoader<String, String>() {
			public String load(String key) throws Exception {
				return adapter.enrich(key);
			}
		};

		cache = CacheBuilder.newBuilder().maximumSize(MAX_CACHE_SIZE)
				.expireAfterWrite(MAX_TIME_RETAIN, TimeUnit.MINUTES)
				.build(loader);
	}

	public void execute(Tuple tuple) {

		String original_message = tuple.getString(0).trim();
		LOG.debug("Received tuple: " + original_message);
		
		Pattern pattern1 = Pattern.compile(originator_ip);
		Pattern pattern2 = Pattern.compile(responder_ip);

		Matcher matcher = pattern1.matcher(original_message);

		String originator_enrichment = "{}";
		String responder_enrichment = "{}";

		try {

			if (matcher.find()) {
				String to_match = matcher.group(1);
				originator_enrichment = cache.getUnchecked(to_match);
				LOG.debug("Found originator_enrichment: " + originator_enrichment + " for " + to_match);
			}

			matcher = pattern2.matcher(original_message);
			if (matcher.find()) {
				String to_match = matcher.group(1);
				responder_enrichment = cache.getUnchecked(to_match);
				LOG.debug("Found responder_enrichment: " + originator_enrichment + " for " + to_match);
			}
			_collector.ack(tuple);
			
		} catch (Exception e) {
			e.printStackTrace();
			_collector.fail(tuple);
		}

		String enriched_message = original_message.substring(0,
				original_message.length() - 1)
				+ ",\""
				+ enrichment_tag
				+ "\":{"
				+ "\"originator\":"
				+ originator_enrichment
				+ ","
				+ "\"responder\":" + responder_enrichment + "}}";
		
		LOG.debug("Setting enriched_message: " + enriched_message);
		
		_collector.emit(new Values(enriched_message));
		
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("message"));
	}

}