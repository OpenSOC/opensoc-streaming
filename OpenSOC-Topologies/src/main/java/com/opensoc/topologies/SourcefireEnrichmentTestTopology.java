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

import storm.kafka.BrokerHosts;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;

import com.enrichments.common.GenericEnrichmentBolt;
import com.opensoc.enrichments.geo.adapters.GeoMysqlAdapter;
import com.opensoc.enrichments.whois.WhoisEnrichmentBolt;
import com.opensoc.enrichments.whois.adapters.WhoisHBaseAdapter;
import com.opensoc.indexing.TelemetryIndexingBolt;
import com.opensoc.indexing.adapters.ESBaseBulkAdapter;
import com.opensoc.parsing.TelemetryParserBolt;
import com.opensoc.parsing.parsers.BasicSourcefireParser;
import com.opensoc.test.spouts.SourcefireTestSpout;

/**
 * This is a basic example of a Storm topology.
 */
public class SourcefireEnrichmentTestTopology {

	public static void main(String[] args) throws Exception {
		TopologyBuilder builder = new TopologyBuilder();

		int parallelism_hint = 1;
		int num_tasks = 1;
		int num_workers = 3;

		String topology_name = "sourcefire";
		int localMode = 1;
		String hdfs_path = "hdfs://172.30.9.110:8020";

		// /--------TODO: what should this be set to?
		BrokerHosts zk_broker_hosts = null;
		String zkRoot = "?";

		Config conf = new Config();
		conf.setDebug(true);

		long MAX_CACHE_SIZE = 10000;
		long MAX_TIME_RETAIN = 10;

		// ------------Geo BOLT configuration

		// ------------Whois BOLT configuration

		conf.put("whois_enrichment_tag", "whois_enrichment");
		conf.put("host_regex", "host\":\"(.*?)\"");
		conf.put("enrichment_source_ip", "172.30.9.108:60000");

		// ------------KAFKA spout configuration

		// SpoutConfig kafkaConfig = new SpoutConfig(zk_broker_hosts,
		// topology_name, zkRoot, topology_name);

		// kafkaConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
		// kafkaConfig.forceFromStart = Boolean.valueOf("True");
		// kafkaConfig.startOffsetTime = -1;

		// builder.setSpout("kafka-spout", new KafkaSpout(kafkaConfig),
		// parallelism_hint)
		// .setNumTasks(1);

		builder.setSpout("EnrichmentSpout", new SourcefireTestSpout(),
				parallelism_hint).setNumTasks(num_tasks);

		// ------------Parser Bolt Configuration

		TelemetryParserBolt parser_bolt = new TelemetryParserBolt()
				.withMessageParser(new BasicSourcefireParser())
				.withOutputFieldName(topology_name);

		builder.setBolt("ParserBolt", parser_bolt, parallelism_hint)
				.shuffleGrouping("EnrichmentSpout").setNumTasks(num_tasks);

		// ------------Geo Enrichment Bolt Configuration


		String geo_enrichment_tag = "geo_enrichment";

		Map<String, Pattern> patterns = new HashMap<String, Pattern>();
		patterns.put("originator_ip_regex", Pattern.compile("ip_src_addr\":\"(.*?)\""));
		patterns.put("responder_ip_regex", Pattern.compile("ip_dst_addr\":\"(.*?)\""));

		GeoMysqlAdapter geo_adapter = new GeoMysqlAdapter("172.30.9.54", 0,
				"test", "test");

		GenericEnrichmentBolt geo_enrichment = new GenericEnrichmentBolt()
				.withEnrichmentTag(geo_enrichment_tag)
				.withOutputFieldName(topology_name).withAdapter(geo_adapter)
				.withMaxTimeRetain(MAX_TIME_RETAIN)
				.withMaxCacheSize(MAX_CACHE_SIZE).withPatterns(patterns);

		builder.setBolt("GeoEnrichBolt", geo_enrichment, parallelism_hint)
				.shuffleGrouping("ParserBolt").setNumTasks(num_tasks);

		// builder.setBolt("WhoisEnrichmentBolt",
		// new WhoisEnrichmentBolt(new WhoisHBaseAdapter()),
		// parallelism_hint).shuffleGrouping("ParserBolt")
		// .setNumTasks(num_tasks);

		// ------------ES BOLT configuration

		/*
		 * String ElasticSearchIP = "172.30.9.148"; int elasticSearchPort =
		 * 9300; String ElasticSearchClusterName = "devo_es"; String
		 * ElasticSearchIndexName = "sourcefire_index"; String
		 * ElasticSearchDocumentName = "sourcefire_doc"; int bulk = 200;
		 * 
		 * TelemetryIndexingBolt indexing_bolt = new TelemetryIndexingBolt()
		 * .withIndexIP(ElasticSearchIP).withIndexPort(elasticSearchPort)
		 * .withClusterName(ElasticSearchClusterName)
		 * .withIndexName(ElasticSearchIndexName)
		 * .withDocumentName(ElasticSearchDocumentName).withBulk(bulk)
		 * .withOutputFieldName(topology_name) .withIndexAdapter(new
		 * ESBaseBulkAdapter());
		 * 
		 * builder.setBolt("IndexingBolt", indexing_bolt, parallelism_hint)
		 * .shuffleGrouping("GeoEnrichBolt").setNumTasks(num_tasks);
		 * 
		 * // ------------HDFS BOLT configuration
		 * 
		 * // FileNameFormat fileNameFormat = new DefaultFileNameFormat() //
		 * .withPath("/" + topology_name + "/"); // RecordFormat format = new
		 * DelimitedRecordFormat() // .withFieldDelimiter("|");
		 * 
		 * // SyncPolicy syncPolicy = new CountSyncPolicy(5); //
		 * FileRotationPolicy rotationPolicy = new FileSizeRotationPolicy(5.0f,
		 * // Units.KB);
		 * 
		 * // HdfsBolt hdfsBolt = new HdfsBolt().withFsUrl(hdfs_path) //
		 * .withFileNameFormat(fileNameFormat).withRecordFormat(format) //
		 * .withRotationPolicy(rotationPolicy).withSyncPolicy(syncPolicy);
		 * 
		 * // builder.setBolt("HDFSBolt", hdfsBolt, parallelism_hint) //
		 * .shuffleGrouping("EnrichmentSpout").setNumTasks(num_tasks);
		 */

		if (localMode == 1) {
			conf.setNumWorkers(1);
			conf.setMaxTaskParallelism(1);
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology(topology_name, conf,
					builder.createTopology());
		} else {

			conf.setNumWorkers(num_workers);
			StormSubmitter.submitTopology(topology_name, conf,
					builder.createTopology());

		}

	}
}