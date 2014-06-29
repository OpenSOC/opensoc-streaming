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

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.opensoc.enrichment.interfaces.GeoAdapter;

@SuppressWarnings({ "rawtypes", "serial" })
public class DualGeoEnrichmentBolt extends AbstractGeoEnrichmentBolt {

	protected transient CacheLoader<String, String> loader;
	protected transient LoadingCache<String, String> cache;

	public DualGeoEnrichmentBolt withGeoAdapter(GeoAdapter adapter) {
		_adapter = adapter;
		return this;
	}

	public DualGeoEnrichmentBolt withOutputFieldName(String OutputFieldName) {
		_OutputFieldName = OutputFieldName;
		return this;
	}

	public DualGeoEnrichmentBolt withSurceIpRegex(String SourceIpRegex) {
		_originator_ip_regex = SourceIpRegex;
		return this;
	}

	public DualGeoEnrichmentBolt withDestIpRegex(String DestIpRegex) {
		_responder_ip_regex = DestIpRegex;
		return this;
	}

	public DualGeoEnrichmentBolt withEnrichmentTag(String EnrichmentTag) {
		_enrichment_tag = EnrichmentTag;
		return this;
	}

	public DualGeoEnrichmentBolt withMaxCacheSize(long MAX_CACHE_SIZE) {
		_MAX_CACHE_SIZE = MAX_CACHE_SIZE;
		return this;
	}

	public DualGeoEnrichmentBolt withMaxTimeRetain(long MAX_TIME_RETAIN) {
		_MAX_TIME_RETAIN = MAX_TIME_RETAIN;
		return this;
	}

	public DualGeoEnrichmentBolt withEnrichmentSourceIP(
			String EnrichmentSourceIP) {
		_enrichment_source_ip = EnrichmentSourceIP;
		return this;
	}

	public void execute(Tuple tuple) {

		String original_message = tuple.getString(0).trim();
		LOG.debug("Received tuple: " + original_message);

		Pattern pattern1 = Pattern.compile(_originator_ip_regex);
		Pattern pattern2 = Pattern.compile(_responder_ip_regex);

		Matcher matcher = pattern1.matcher(original_message);

		String originator_enrichment = "{}";
		String responder_enrichment = "{}";

		try {

			if (matcher.find()) {
				String to_match = matcher.group(1);
				originator_enrichment = cache.getUnchecked(to_match);
				LOG.debug("Found originator_enrichment: "
						+ originator_enrichment + " for " + to_match);
			}

			matcher = pattern2.matcher(original_message);
			if (matcher.find()) {
				String to_match = matcher.group(1);
				responder_enrichment = cache.getUnchecked(to_match);
				LOG.debug("Found responder_enrichment: "
						+ originator_enrichment + " for " + to_match);
			}
			_collector.ack(tuple);

		} catch (Exception e) {
			e.printStackTrace();
			_collector.fail(tuple);
		}

		String enriched_message = original_message.substring(0,
				original_message.length() - 1)
				+ ",\""
				+ _enrichment_tag
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

	
	@Override
	void doPrepare(Map conf, TopologyContext topologyContext,
			OutputCollector collector) throws IOException {
		LOG.info("Preparing GeoEnrichmentBolt Bolt...");

		_collector = collector;

		boolean success = _adapter.initializeAdapter(_enrichment_source_ip);

		if (!success) {
			LOG.error("GeoEnrichmentBolt could not initialize adapter");
		}

		LOG.info("GeoEnrichmentBolt Initialized...");

		loader = new CacheLoader<String, String>() {
			public String load(String key) throws Exception {
				return _adapter.enrich(key);
			}
		};

		cache = CacheBuilder.newBuilder().maximumSize(_MAX_CACHE_SIZE)
				.expireAfterWrite(_MAX_TIME_RETAIN, TimeUnit.MINUTES)
				.build(loader);

	}

}