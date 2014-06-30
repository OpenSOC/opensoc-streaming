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

package com.opensoc.topologies;

import java.util.HashMap;
import java.util.Map;

import org.apache.storm.hdfs.bolt.HdfsBolt;
import org.apache.storm.hdfs.bolt.format.DefaultFileNameFormat;
import org.apache.storm.hdfs.bolt.format.DelimitedRecordFormat;
import org.apache.storm.hdfs.bolt.format.FileNameFormat;
import org.apache.storm.hdfs.bolt.format.RecordFormat;
import org.apache.storm.hdfs.bolt.rotation.FileRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy.Units;
import org.apache.storm.hdfs.bolt.sync.CountSyncPolicy;
import org.apache.storm.hdfs.bolt.sync.SyncPolicy;

import storm.kafka.BrokerHosts;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;
import storm.kafka.bolt.KafkaBolt;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;

import com.opensoc.enrichments.cif.CIFEnrichmentBolt;
import com.opensoc.enrichments.cif.adapters.CIFHbaseAdapter;
import com.opensoc.enrichments.lancope.LancopeEnrichmentBolt;
import com.opensoc.enrichments.lancope.adapters.LancopeHbaseAdapter;
import com.opensoc.indexing.TelemetryIndexingBolt;
import com.opensoc.indexing.adapters.ESBaseBulkAdapter;
import com.opensoc.parsing.TelemetryParserBolt;
import com.opensoc.parsing.parsers.BasicBroParser;

/**
 * This is a basic example of a Storm topology.
 */

public class BroEnrichmentTestTopology {

	public static void main(String[] args) throws Exception {
		TopologyBuilder builder = new TopologyBuilder();

		String topology_name = "bro";
		int parallelism_hint = 1;
		int num_tasks = 1;
		int localMode = 1;
		String hdfs_path = "hdfs://172.30.9.110:8020";

		Config conf = new Config();
		conf.setDebug(true);

		// ------------KAFKA spout configuration

		BrokerHosts zk = new ZkHosts("172.30.9.115:2181");

		SpoutConfig kafkaConfig = new SpoutConfig(zk, "metadata", "/", "bro");

		kafkaConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
		kafkaConfig.forceFromStart = Boolean.valueOf("True");
		kafkaConfig.startOffsetTime = -1;

		builder.setSpout("kafka-spout", new KafkaSpout(kafkaConfig),
				parallelism_hint).setNumTasks(1);

		// ------------ParserBolt configuration

		TelemetryParserBolt parser_bolt = new TelemetryParserBolt()
				.withMessageParser(new BasicBroParser()).withOutputFieldName(
						topology_name);

		builder.setBolt("ParserBolt", parser_bolt, parallelism_hint)
				.shuffleGrouping("EnrichmentSpout").setNumTasks(num_tasks);

		// ------------CIF bolt configuration

		CIFEnrichmentBolt cif_enrichment = new CIFEnrichmentBolt().withAdapter(
				new CIFHbaseAdapter()).withOutputFieldName(topology_name);

		builder.setBolt("CIFEnrichmentBolt", cif_enrichment, parallelism_hint)
				.shuffleGrouping("ParserBolt").setNumTasks(num_tasks);

		// ------------Lancope bolt configuration

		LancopeEnrichmentBolt lancope_enrichment = new LancopeEnrichmentBolt()
				.withAdapter(new LancopeHbaseAdapter()).withOutputFieldName(
						topology_name);

		builder.setBolt("LancopeEnrichmentBolt", lancope_enrichment,
				parallelism_hint).shuffleGrouping("CIFEnrichmentBolt")
				.setNumTasks(num_tasks);

		// ------------Kafka Bolt Configuration

		Map<String, String> kafka_broker_properties = new HashMap<String, String>();
		// add some properties?

		conf.put("KAFKA_BROKER_PROPERTIES", kafka_broker_properties);
		conf.put("TOPIC", topology_name + "_cnt");

		builder.setBolt("KafkaBolt", new KafkaBolt<String, String>(),
				parallelism_hint).shuffleGrouping("LancopeEnrichmentBolt")
				.setNumTasks(num_tasks);

		// ------------ES BOLT configuration

		String ElasticSearchIP = "172.30.9.148";
		int elasticSearchPort = 9300;
		String ElasticSearchClusterName = "devo_es";
		String ElasticSearchIndexName = "bro_index";
		String ElasticSearchDocumentName = "bro_doc";
		int bulk = 200;

		TelemetryIndexingBolt indexing_bolt = new TelemetryIndexingBolt()
				.withIndexIP(ElasticSearchIP).withIndexPort(elasticSearchPort)
				.withClusterName(ElasticSearchClusterName)
				.withIndexName(ElasticSearchIndexName)
				.withDocumentName(ElasticSearchDocumentName).withBulk(bulk)
				.withOutputFieldName(topology_name)
				.withIndexAdapter(new ESBaseBulkAdapter());

		builder.setBolt("IndexingBolt", indexing_bolt, parallelism_hint)
				.shuffleGrouping("LancopeEnrichmentBolt")
				.setNumTasks(num_tasks);

		// ------------HDFS BOLT configuration

		FileNameFormat fileNameFormat = new DefaultFileNameFormat()
				.withPath("/" + topology_name + "/");
		RecordFormat format = new DelimitedRecordFormat()
				.withFieldDelimiter("|");

		SyncPolicy syncPolicy = new CountSyncPolicy(5);
		FileRotationPolicy rotationPolicy = new FileSizeRotationPolicy(5.0f,
				Units.KB);

		HdfsBolt hdfsBolt = new HdfsBolt().withFsUrl(hdfs_path)
				.withFileNameFormat(fileNameFormat).withRecordFormat(format)
				.withRotationPolicy(rotationPolicy).withSyncPolicy(syncPolicy);

		builder.setBolt("HDFSBolt", hdfsBolt, parallelism_hint)
				.shuffleGrouping("EnrichmentSpout").setNumTasks(num_tasks);

		if (localMode == 1) {
			conf.setNumWorkers(1);

			StormSubmitter.submitTopology(args[0], conf,
					builder.createTopology());
		} else {

			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology("test", conf, builder.createTopology()); //
			Utils.sleep(10000); // cluster.killTopology("test");
			cluster.shutdown();
		}

	}
}
