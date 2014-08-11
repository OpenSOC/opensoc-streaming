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
package com.opensoc.parsing;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.base.BaseRichBolt;

import com.codahale.metrics.Counter;
import com.opensoc.metrics.MetricReporter;
import com.opensoc.parser.interfaces.MessageParser;

@SuppressWarnings("rawtypes")
public abstract class AbstractParserBolt extends BaseRichBolt {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6710596708304282838L;

	protected static final Logger LOG = LoggerFactory
			.getLogger(AbstractParserBolt.class);

	protected OutputCollector _collector;
	protected MessageParser _parser;

	protected String OutputFieldName;
	protected MetricReporter _reporter;

	protected Counter ackCounter, emitCounter, failCounter;

	protected void registerCounters() {

		String ackString = _parser.getClass().getSimpleName() + ".ack";

		String emitString = _parser.getClass().getSimpleName() + ".emit";

		String failString = _parser.getClass().getSimpleName() + ".fail";

		ackCounter = _reporter.registerCounter(ackString);
		emitCounter = _reporter.registerCounter(emitString);
		failCounter = _reporter.registerCounter(failString);

	}

	public final void prepare(Map conf, TopologyContext topologyContext,
			OutputCollector collector) {
		_collector = collector;
		if (this._parser == null)
			throw new IllegalStateException("MessageParser must be specified");
		if (this.OutputFieldName == null)
			throw new IllegalStateException("OutputFieldName must be specified");

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