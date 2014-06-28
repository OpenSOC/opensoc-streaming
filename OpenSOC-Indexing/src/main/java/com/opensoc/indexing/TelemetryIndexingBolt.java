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
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

import com.opensoc.index.interfaces.IndexAdapter;

@SuppressWarnings("serial")
public class TelemetryIndexingBolt extends AbstractIndexingBolt {

	public TelemetryIndexingBolt withIndexIP(String IndexIP) {
		_IndexIP = IndexIP;
		return this;
	}

	public TelemetryIndexingBolt withIndexPort(int IndexPort) {
		_IndexPort = IndexPort;
		return this;
	}
	
	public TelemetryIndexingBolt withIndexName(String IndexName) {
		_IndexName = IndexName;
		return this;
	}

	public TelemetryIndexingBolt withClusterName(String ClusterName) {
		_ClusterName = ClusterName;
		return this;
	}

	public TelemetryIndexingBolt withDocumentName(String DocumentName) {
		_DocumentName = DocumentName;
		return this;
	}

	public TelemetryIndexingBolt withBulk(int BulkIndexNumber) {
		_BulkIndexNumber = BulkIndexNumber;
		return this;
	}

	public TelemetryIndexingBolt withOutputFieldName(String OutputFieldName) {
		this.OutputFieldName = OutputFieldName;
		return this;
	}

	public TelemetryIndexingBolt withIndexAdapter(IndexAdapter adapter) {
		this._adapter = adapter;
		return this;
	}

	@SuppressWarnings("rawtypes")
	@Override
	void doPrepare(Map conf, TopologyContext topologyContext,
			OutputCollector collector) throws IOException {

		boolean success = this._adapter.initializeConnection(_IndexIP,
				_IndexPort, _ClusterName, _IndexName, _DocumentName,
				_BulkIndexNumber);

		if (!success)
			throw new IllegalStateException(
					"Could not initialize index adapter");
	}

	public void execute(Tuple tuple) {

		String message = tuple.getString(0);

		LOG.debug("Received message: " + message);

		boolean success = _adapter.bulkIndex(message);

		if (success)
			_collector.ack(tuple);
		else
			_collector.fail(tuple);

	}

	public void declareOutputFields(OutputFieldsDeclarer declearer) {
		declearer.declare(new Fields(this.OutputFieldName));
	}

}

/*
 * @SuppressWarnings("serial") public class TelemetryIndexingBolt extends
 * BaseRichBolt {
 * 
 * private static final Logger LOG =
 * LoggerFactory.getLogger(TelemetryIndexingBolt.class);
 * 
 * OutputCollector _collector; IndexAdapter _adapter;
 * 
 * public TelemetryIndexingBolt(IndexAdapter adapter) { _adapter = adapter; }
 * 
 * @SuppressWarnings("rawtypes") public void prepare(Map conf, TopologyContext
 * context, OutputCollector collector) {
 * 
 * LOG.info("Initializing IndexingBolt...");
 * 
 * _collector = collector;
 * 
 * String ip = (String) conf.get("es_ip"); int port = ((Long)
 * conf.get("es_port")).intValue(); String cluster_name = (String)
 * conf.get("es_cluster_name"); String index_name = (String)
 * conf.get("index_name"); String document_name = (String)
 * conf.get("document_name"); int bulk = ((Long)
 * conf.get("es_bulk")).intValue();
 * 
 * LOG.debug("Setting ip: " + ip); LOG.debug("Setting port: " + port);
 * LOG.debug("Setting cluster_name: " + cluster_name);
 * LOG.debug("Setting index_name: " + index_name);
 * LOG.debug("Setting document_name: " + document_name);
 * LOG.debug("Setting bulk: " + bulk);
 * 
 * LOG.debug("Initializing adapter...");
 * 
 * boolean success = _adapter.initializeConnection(ip, port, cluster_name,
 * index_name, document_name, bulk, LOG);
 * 
 * if(!success) LOG.error("Failed to initialize adapter...");
 * 
 * LOG.info("Indexing bolt initialized..."); }
 * 
 * public void execute(Tuple tuple) {
 * 
 * String message = tuple.getString(0);
 * 
 * LOG.debug("Received message: " + message);
 * 
 * boolean success = _adapter.bulkIndex(message);
 * 
 * if (success) _collector.ack(tuple); else _collector.fail(tuple);
 * 
 * }
 * 
 * public void declareOutputFields(OutputFieldsDeclarer arg0) { // TODO
 * Auto-generated method stub
 * 
 * }
 * 
 * }
 */
