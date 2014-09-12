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

import org.apache.commons.configuration.Configuration;
import org.json.simple.JSONObject;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.opensoc.json.serialization.JSONEncoderHelper;
import com.opensoc.metrics.MetricReporter;
import com.opensoc.parser.interfaces.MessageFilter;
import com.opensoc.parser.interfaces.MessageParser;

/**
 * Uses an adapter to parse a telemetry message from its native format into a
 * standard JSON. For a list of available adapter please check
 * com.opensoc.parser.parsers. The input is a raw byte array and the output is a
 * JSONObject
 * <p>
 * The parsing conventions are as follows:
 * <p>
 * <ul>
 * 
 * <li>ip_src_addr = source ip of a message
 * <li>ip_dst_addr = destination ip of a message
 * <li>ip_src_port = source port of a message
 * <li>ip_dst_port = destination port of a message
 * <li>protocol = protocol of a message
 * <ul>
 * <p>
 * <p>
 * If a message does not contain at least one of these variables it will be
 * failed
 **/

@SuppressWarnings("rawtypes")
public class TelemetryParserBolt extends AbstractParserBolt {

	private static final long serialVersionUID = -2647123143398352020L;
	private JSONObject metricConfiguration;

	/**
	 * @param parser
	 *            The parser class for parsing the incoming raw message byte
	 *            stream
	 * @return Instance of this class
	 */

	public TelemetryParserBolt withMessageParser(MessageParser parser) {
		_parser = parser;
		return this;
	}

	/**
	 * @param OutputFieldName
	 *            Field name of the output tuple
	 * @return Instance of this class
	 */

	public TelemetryParserBolt withOutputFieldName(String OutputFieldName) {
		this.OutputFieldName = OutputFieldName;
		return this;
	}

	/**
	 * @param filter
	 *            A class for filtering/dropping incomming telemetry messages
	 * @return Instance of this class
	 */

	public TelemetryParserBolt withMessageFilter(MessageFilter filter) {
		this._filter = filter;
		return this;
	}

	/**
	 * @param config
	 *            A class for generating custom metrics into graphite
	 * @return Instance of this class
	 */

	public TelemetryParserBolt withMetricConfig(Configuration config) {
		this.metricConfiguration = JSONEncoderHelper.getJSON(config
				.subset("com.opensoc.metrics"));
		return this;
	}

	@Override
	void doPrepare(Map conf, TopologyContext topologyContext,
			OutputCollector collector) throws IOException {

		LOG.info("[OpenSOC] Preparing TelemetryParser Bolt...");

		if (metricConfiguration != null) {
			_reporter = new MetricReporter();
			_reporter
					.initialize(metricConfiguration, TelemetryParserBolt.class);
			LOG.info("[OpenSOC] Metric reporter is initialized");
		} else {
			LOG.info("[OpenSOC] Metric reporter is not initialized");
		}
		this.registerCounters();
	}

	@SuppressWarnings("unchecked")
	public void execute(Tuple tuple) {

		LOG.trace("[OpenSOC] Starting to process a new incoming tuple");

		byte[] original_message = null;

		try {
			original_message = tuple.getBinary(0);
			LOG.trace("[OpenSOC] Successfully read the incoming tuple");
		} catch (Exception e) {
			LOG.error("[OpenSOC] Unable to read the incoming tuple");
			e.printStackTrace();
			_collector.fail(tuple);

			if (metricConfiguration != null)
				failCounter.inc();
		}

		try {

			LOG.trace("[OpenSOC] Starting the parsing process");

			if (original_message == null || original_message.length == 0) {
				LOG.error("Incomming tuple is null");
				throw new Exception("Invalid message length");
			}

			LOG.trace("[OpenSOC] Attempting to transofrm binary message to JSON");
			JSONObject transformed_message = _parser.parse(original_message);
			LOG.debug("[OpenSOC] Transformed Telemetry message: "
					+ transformed_message);

			if (transformed_message == null || transformed_message.isEmpty())
				throw new Exception("Unable to turn binary message into a JSON");

			JSONObject new_message = new JSONObject();

			LOG.trace("[OpenSOC] Checking if the transformed JSON conforms to the right schema");

			if (!checkForSchemaCorrectness(transformed_message)) {
				_collector.fail(tuple);

				if (metricConfiguration != null)
					failCounter.inc();

				throw new Exception("Incorrect formatting on message: "
						+ transformed_message);
			}

			LOG.trace("[OpenSOC] JSON message has the right schema");

			new_message.put("message", transformed_message);
			_collector.ack(tuple);

			if (metricConfiguration != null)
				ackCounter.inc();

			if (_filter != null) {
				if (_filter.emitTuple(transformed_message)) {
					LOG.debug("[OpenSOC] Mesage is not filtered: "
							+ transformed_message);
					_collector.emit(new Values(new_message));

					if (metricConfiguration != null)
						emitCounter.inc();
				} else {
					LOG.debug("[OpenSOC] Mesage is filtered: "
							+ transformed_message);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Failed to parse telemetry message :" + original_message);
			_collector.fail(tuple);

			if (metricConfiguration != null)
				failCounter.inc();
		}
	}

	public void declareOutputFields(OutputFieldsDeclarer declearer) {
		declearer.declare(new Fields(this.OutputFieldName));

	}
}