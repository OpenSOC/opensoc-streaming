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

package com.opensoc.enrichment.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.Configuration;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensoc.json.serialization.JSONEncoderHelper;
import com.opensoc.metrics.MetricReporter;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

@SuppressWarnings({ "rawtypes", "serial" })
public class GenericEnrichmentBolt extends AbstractEnrichmentBolt {

	private static final Logger LOG = LoggerFactory
			.getLogger(GenericEnrichmentBolt.class);
	private JSONObject metricConfiguration;

	public GenericEnrichmentBolt withAdapter(EnrichmentAdapter adapter) {
		_adapter = adapter;
		return this;
	}

	public GenericEnrichmentBolt withOutputFieldName(String OutputFieldName) {
		_OutputFieldName = OutputFieldName;
		return this;
	}

	public GenericEnrichmentBolt withEnrichmentTag(String EnrichmentTag) {
		_enrichment_tag = EnrichmentTag;
		return this;
	}

	public GenericEnrichmentBolt withMaxCacheSize(long MAX_CACHE_SIZE) {
		_MAX_CACHE_SIZE = MAX_CACHE_SIZE;
		return this;
	}

	public GenericEnrichmentBolt withMaxTimeRetain(long MAX_TIME_RETAIN) {
		_MAX_TIME_RETAIN = MAX_TIME_RETAIN;
		return this;
	}

	public GenericEnrichmentBolt withEnrichmentSourceIP(
			String EnrichmentSourceIP) {
		_enrichment_source_ip = EnrichmentSourceIP;
		return this;
	}

	public GenericEnrichmentBolt withKeys(List<String> jsonKeys) {
		_jsonKeys = jsonKeys;
		return this;
	}

	public GenericEnrichmentBolt withMetricConfiguration(Configuration config) {
		this.metricConfiguration = JSONEncoderHelper.getJSON(config
				.subset("com.opensoc.metrics"));
		return this;
	}

	@SuppressWarnings("unchecked")
	public void execute(Tuple tuple) {

		JSONObject original_message = (JSONObject) tuple.getValue(0);
		LOG.debug("Received tuple: " + original_message);

		LOG.debug("---------RECEIVED MESSAGE IN ENRICHMENT BOLT: "
				+ original_message);

		for (Object key : original_message.keySet()) {
			JSONObject payload = (JSONObject) original_message.get(key);
			Map<String, JSONObject> tokens_found = new HashMap<String, JSONObject>();

			for (String jsonkey : _jsonKeys) {

				LOG.debug("Processing:" + jsonkey + " within:" + payload);

				String jsonvalue = (String) payload.get(jsonkey);

				LOG.debug("---------Processing: " + jsonkey + " -> "
						+ jsonvalue);

				if (null == jsonvalue)
					continue;

				JSONObject enrichment = cache.getUnchecked(jsonvalue);

				LOG.debug("---------Enriched: " + jsonkey + " -> " + enrichment);

				tokens_found.put(jsonkey + "_enriched", enrichment);
			}

			payload.putAll(tokens_found);
			original_message.put(key, payload);

		}

		LOG.debug("-----------------combined: " + original_message);

		_collector.emit(new Values(original_message));
		emitCounter.inc();
		_collector.ack(tuple);

		ackCounter.inc();

	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("message"));
	}

	@Override
	void doPrepare(Map conf, TopologyContext topologyContext,
			OutputCollector collector) throws IOException {
		LOG.info("Preparing Enrichment Bolt...");

		_collector = collector;
		_reporter = new MetricReporter();
		_reporter.initialize(metricConfiguration, GenericEnrichmentBolt.class);
		this.registerCounters();

		LOG.info("Enrichment bolt initialized...");
	}

}