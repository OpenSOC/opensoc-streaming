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

package com.opensoc.indexing;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.json.simple.JSONObject;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.opensoc.helpers.topology.ErrorGenerator;
import com.opensoc.index.interfaces.IndexAdapter;
import com.opensoc.json.serialization.JSONEncoderHelper;
import com.opensoc.metrics.MetricReporter;

/**
 * 
 * Bolt for indexing telemetry messages into Elastic Search, Solr, Druid, etc...
 * For a list of all adapters provided please see com.opensoc.indexing.adapters
 * 
 * As of release of this code the following adapters for indexing are provided:
 * <p>
 * <ul>
 * 
 * <li>ESBulkAdapter = adapter that can bulk index messages into ES
 * <li>ESBulkRotatingAdapter = adapter that can bulk index messages into ES,
 * rotate the index, and apply an alias to the rotated index
 * <ul>
 * <p>
 *
 */

@SuppressWarnings("serial")
public class TelemetryIndexingBolt extends AbstractIndexingBolt {

	private JSONObject metricConfiguration;
	private String _indexDateFormat;
	
	private Set<Tuple> tuple_queue = new HashSet<Tuple>();

	/**
	 * 
	 * @param IndexIP
	 *            ip of ElasticSearch/Solr/etc...
	 * @return instance of bolt
	 */
	public TelemetryIndexingBolt withIndexIP(String IndexIP) {
		_IndexIP = IndexIP;
		return this;
	}

	/**
	 * 
	 * @param IndexPort
	 *            port of ElasticSearch/Solr/etc...
	 * @return instance of bolt
	 */

	public TelemetryIndexingBolt withIndexPort(int IndexPort) {
		_IndexPort = IndexPort;
		return this;
	}

	/**
	 * 
	 * @param IndexName
	 *            name of the index in ElasticSearch/Solr/etc...
	 * @return instance of bolt
	 */
	public TelemetryIndexingBolt withIndexName(String IndexName) {
		_IndexName = IndexName;
		return this;
	}

	/**
	 * 
	 * @param ClusterName
	 *            name of cluster to index into in ElasticSearch/Solr/etc...
	 * @return instance of bolt
	 */
	public TelemetryIndexingBolt withClusterName(String ClusterName) {
		_ClusterName = ClusterName;
		return this;
	}

	/**
	 * 
	 * @param DocumentName
	 *            name of document to be indexed in ElasticSearch/Solr/etc...
	 * @return
	 */

	public TelemetryIndexingBolt withDocumentName(String DocumentName) {
		_DocumentName = DocumentName;
		return this;
	}

	/**
	 * 
	 * @param BulkIndexNumber
	 *            number of documents to bulk index together
	 * @return instance of bolt
	 */
	public TelemetryIndexingBolt withBulk(int BulkIndexNumber) {
		_BulkIndexNumber = BulkIndexNumber;
		return this;
	}

	/**
	 * 
	 * @param adapter
	 *            adapter that handles indexing of JSON strings
	 * @return instance of bolt
	 */
	public TelemetryIndexingBolt withIndexAdapter(IndexAdapter adapter) {
		_adapter = adapter;

		return this;
	}
	
	/**
	 * 
	 * @param dateFormat
	 *           timestamp to append to index names
	 * @return instance of bolt
	 */
	public TelemetryIndexingBolt withIndexTimestamp(String indexTimestamp) {
		_indexDateFormat = indexTimestamp;

		return this;
	}
	/**
	 * 
	 * @param config
	 *            - configuration for pushing metrics into graphite
	 * @return instance of bolt
	 */
	public TelemetryIndexingBolt withMetricConfiguration(Configuration config) {
		this.metricConfiguration = JSONEncoderHelper.getJSON(config
				.subset("com.opensoc.metrics"));
		return this;
	}

	@SuppressWarnings("rawtypes")
	@Override
	void doPrepare(Map conf, TopologyContext topologyContext,
			OutputCollector collector) throws IOException {

		try {
			
			_adapter.initializeConnection(_IndexIP, _IndexPort,
					_ClusterName, _IndexName, _DocumentName, _BulkIndexNumber, _indexDateFormat);
			
			_reporter = new MetricReporter();
			_reporter.initialize(metricConfiguration,
					TelemetryIndexingBolt.class);
			this.registerCounters();
		} catch (Exception e) {
			
			e.printStackTrace();
					
			JSONObject error = ErrorGenerator.generateErrorMessage(new String("bulk index problem"), e);
			_collector.emit("error", new Values(error));
		}

	}

	public void execute(Tuple tuple) {

		JSONObject message = null;

		try {
			LOG.trace("[OpenSOC] Indexing bolt gets:  " + message);

			message = (JSONObject) tuple.getValueByField("message");

			if (message == null || message.isEmpty())
				throw new Exception(
						"Could not parse message from binary stream");

			int result_code = _adapter.bulkIndex(message);

			if (result_code == 0) {
				tuple_queue.add(tuple);
			} else if (result_code == 1) {
				tuple_queue.add(tuple);
				
				Iterator<Tuple> iterator = tuple_queue.iterator();
				while(iterator.hasNext())
				{
					Tuple setElement = iterator.next();
					_collector.ack(setElement);
					ackCounter.inc();
				}
				tuple_queue.clear();
			} else if (result_code == 2) {
				throw new Exception("Failed to index elements with client");
			}

		} catch (Exception e) {
			e.printStackTrace();
			
			
			Iterator<Tuple> iterator = tuple_queue.iterator();
			while(iterator.hasNext())
			{
				Tuple setElement = iterator.next();
				_collector.fail(setElement);
				failCounter.inc();
				
				
				JSONObject error = ErrorGenerator.generateErrorMessage(new String("bulk index problem"), e);
				_collector.emit("error", new Values(error));
			}
			tuple_queue.clear();

			
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declearer) {
		declearer.declareStream("error", new Fields("Index"));
	}

}