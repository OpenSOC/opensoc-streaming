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


package com.opensoc.parsing;

import java.io.IOException;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.opensoc.parser.interfaces.MessageParser;


@SuppressWarnings("rawtypes")
public class TelemetryParserBolt extends AbstractParserBolt {

	/**
	 * OpenSOC telemetry parsing bolt with JSON output
	 */
	private static final long serialVersionUID = -2647123143398352020L;


	public TelemetryParserBolt withMessageParser(MessageParser parser) {
		_parser = parser;
		return this;
	}

	public TelemetryParserBolt withOutputFieldName(String OutputFieldName) {
		this.OutputFieldName = OutputFieldName;
		return this;
	}
	
	@Override
	void doPrepare(Map conf, TopologyContext topologyContext,
			OutputCollector collector) throws IOException {

		LOG.info("Preparing TelemetryParser Bolt...");
	}

	public void execute(Tuple tuple) {
		String original_mesasge = tuple.getString(0);

		try {

			LOG.debug("Original Telemetry message: " + original_mesasge);

			String transformed_message = _parser.parse(original_mesasge);
			LOG.debug("Transformed Telemetry message: " + transformed_message);

			_collector.ack(tuple);
			_collector.emit(new Values(transformed_message));

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Failed to parse telemetry message :" + original_mesasge);
			_collector.fail(tuple);
		}
	}
	
	public void declareOutputFields(OutputFieldsDeclarer declearer) {
		declearer.declare(new Fields(this.OutputFieldName));

	}
}