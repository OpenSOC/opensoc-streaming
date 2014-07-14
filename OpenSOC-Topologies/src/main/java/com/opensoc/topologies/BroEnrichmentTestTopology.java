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
import java.util.regex.Pattern;

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

import com.opensoc.enrichment.adapters.cif.CIFHbaseAdapter;
import com.opensoc.enrichment.adapters.lancope.LancopeHbaseAdapter;
import com.opensoc.enrichment.common.GenericEnrichmentBolt;
import com.opensoc.indexing.TelemetryIndexingBolt;
import com.opensoc.indexing.adapters.ESBaseBulkAdapter;
import com.opensoc.parsing.AbstractParserBolt;
import com.opensoc.parsing.TelemetryParserBolt;
import com.opensoc.parsing.parsers.BasicBroParser;
import com.opensoc.test.spouts.GenericInternalTestSpout;

/**
 * This is a basic example of a Storm topology.
 */

public class BroEnrichmentTestTopology {

	public static void main(String[] args) throws Exception {
		TopologyBuilder builder = new TopologyBuilder();

		String topology_name = "bro";
		int parallelism_hint = 1;
		int num_tasks = 1;
	//	int localMode = 0;
		//String hdfs_path = "hdfs://192.168.161.128:8020";
		
		int localMode = 0;
		String hdfs_path = "hdfs://172.30.9.110:8020";

		long MAX_CACHE_SIZE = 10000;
		long MAX_TIME_RETAIN = 10;

		Config conf = new Config();
		conf.setDebug(true);

		// ------------KAFKA spout configuration
/*
		BrokerHosts zk = new ZkHosts("192.168.161.128:2181");

		SpoutConfig kafkaConfig = new SpoutConfig(zk, "metadata", "/", "bro");

		kafkaConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
		kafkaConfig.forceFromStart = Boolean.valueOf("True");
		kafkaConfig.startOffsetTime = -1;
*/
	//	builder.setSpout("kafka-spout", new KafkaSpout(kafkaConfig),
		//		parallelism_hint).setNumTasks(1);

		//EnrichmentSpout
		GenericInternalTestSpout testSpout = new GenericInternalTestSpout().withFilename("BroExampleOutput").withRepeating(false);

		builder.setSpout("EnrichmentSpout", testSpout,
					parallelism_hint).setNumTasks(1);
		
		// ------------ParserBolt configuration

		AbstractParserBolt parser_bolt = new TelemetryParserBolt()
				.withMessageParser(new BasicBroParser()).withOutputFieldName(
						topology_name);

		builder.setBolt("ParserBolt", parser_bolt, parallelism_hint)
				.shuffleGrouping("EnrichmentSpout").setNumTasks(num_tasks);

		// ------------CIF bolt configuration

		Map<String, Pattern> cif_patterns = new HashMap<String, Pattern>();
		Map<String, Integer> cif_pattern_ids = new HashMap<String, Integer>();
		cif_patterns.put("IP_Address", Pattern.compile("(id\\..*?:\\\")(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})"));
		cif_pattern_ids.put("IP_Address", 2);
		
		GenericEnrichmentBolt cif_enrichment = new GenericEnrichmentBolt()
				.withAdapter(new CIFHbaseAdapter())
				.withOutputFieldName(topology_name)
				.withOutputFieldName(topology_name)
				.withEnrichmentTag("CIF_Enrichment")
				.withMaxTimeRetain(MAX_TIME_RETAIN)
				.withMaxCacheSize(MAX_CACHE_SIZE).withPatterns(cif_patterns).withPatternIDs(cif_pattern_ids);

		builder.setBolt("CIFEnrichmentBolt", cif_enrichment, parallelism_hint)
				.shuffleGrouping("ParserBolt").setNumTasks(num_tasks);

		// ------------Lancope bolt configuration

		Map<String, Pattern> lancope_patterns = new HashMap<String, Pattern>();
		lancope_patterns.put("somepattern", Pattern.compile("somevalue"));
		lancope_patterns.put("somepattern", Pattern.compile("somevalue"));

		GenericEnrichmentBolt lancope_enrichment = new GenericEnrichmentBolt()
				.withAdapter(new LancopeHbaseAdapter())
				.withOutputFieldName(topology_name)
				.withEnrichmentTag("sometag")
				.withMaxTimeRetain(MAX_TIME_RETAIN)
				.withMaxCacheSize(MAX_CACHE_SIZE)
				.withPatterns(lancope_patterns);

		builder.setBolt("LancopeEnrichmentBolt", lancope_enrichment,
				parallelism_hint).shuffleGrouping("CIFEnrichmentBolt")
				.setNumTasks(num_tasks);
		// ------------Kafka Bolt Configuration

	/*	Map<String, String> kafka_broker_properties = new HashMap<String, String>();
		// add some properties?

		conf.put("KAFKA_BROKER_PROPERTIES", kafka_broker_properties);
		conf.put("TOPIC", topology_name + "_cnt");

		builder.setBolt("KafkaBolt", new KafkaBolt<String, String>(),
				parallelism_hint).shuffleGrouping("LancopeEnrichmentBolt")
				.setNumTasks(num_tasks);
*/
		// ------------ES BOLT configuration

		String ElasticSearchIP = "192.168.161.128";
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
