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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

@SuppressWarnings({ "rawtypes", "serial" })
public class GenericEnrichmentBolt extends AbstractEnrichmentBolt {

	private Map<String, Integer> _patternids;
	private boolean _withPatternIDs = false;

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

	public GenericEnrichmentBolt withPatterns(Map<String, Pattern> patterns) {
		_patterns = patterns;
		return this;
	}

	/**
	 * @param patternids
	 * @return
	 * Setting groupID's within each pattern.
	 * If this is not set, groupID will default to 1
	 */
	public GenericEnrichmentBolt withPatternIDs(Map<String, Integer> patternids) {
		_patternids = patternids;
		_withPatternIDs = true;
		return this;
	}

	@SuppressWarnings("unchecked")
	public void execute(Tuple tuple) {

		String original_message = tuple.getString(0).trim();
		LOG.debug("Received tuple: " + original_message);

		Map<String, JSONObject> tokens_found = new HashMap<String, JSONObject>();
		JSONObject combined_enrichment = new JSONObject();

		System.out.println("---------RECEIVED MESSAGE IN GEO BOLT: "
				+ original_message);

		for (String pattern_name : _patterns.keySet()) {
			Pattern pattern = _patterns.get(pattern_name);
			Matcher matcher = pattern.matcher(original_message);

			System.out.println("Processing:" + pattern_name);

			while (matcher.find()) {

				//groupID represents the specific group within regex which represents lookup key
				int groupID = 1;
				if (_withPatternIDs)
					groupID = _patternids.get(pattern_name);

				String to_match = matcher.group(groupID);
				System.out.println("---------Found Token: " + to_match);
				
				JSONObject enrichment = cache.getUnchecked(to_match);
				System.out.println("---------Enrichment for Token: "
						+ enrichment);
				System.out.println("---------Putting : " + pattern_name + "_"
						+ to_match + ":" + enrichment);
				tokens_found.put(pattern_name + "_" + to_match, enrichment);
			}
		}

		combined_enrichment.putAll(tokens_found);
		System.out.println("-----------------Enrichment: "
				+ combined_enrichment);
		String enriched_message = original_message.substring(0,
				original_message.length() - 1)
				+ ",\""
				+ _enrichment_tag
				+ "\":" + combined_enrichment + "}";

		System.out.println("-----------------combined: " + enriched_message);

		// LOG.debug("Setting enriched_message: " + enriched_message);

		// _collector.emit(new Values(enriched_message));

	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("message"));
	}

	@Override
	void doPrepare(Map conf, TopologyContext topologyContext,
			OutputCollector collector) throws IOException {
		LOG.info("Preparing Enrichment Bolt...");

		_collector = collector;

		LOG.info("Enrichment bolt initialized...");
	}

}