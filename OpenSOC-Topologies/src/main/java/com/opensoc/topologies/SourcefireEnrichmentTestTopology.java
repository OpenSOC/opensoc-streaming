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
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;

import com.opensoc.enrichments.geo.GeoEnrichmentBolt;
import com.opensoc.enrichments.geo.adapters.GeoMysqlAdapter;
import com.opensoc.indexing.IndexingBolt;
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

		conf.put("MAX_CACHE_SIZE", 10000);
		conf.put("MAX_TIME_RETAIN", 10);

		// ------------Geo BOLT configuration

		conf.put("geo_enrichment_source_ip", "172.30.9.54");
		conf.put("originator_ip_regex", "ip_src_addr\":\"(.*?)\"");
		conf.put("responder_ip_regex", "ip_dst_addr\":\"(.*?)\"");
		conf.put("geo_enrichment_tag", "geo_enrichment");

		// ------------Whois BOLT configuration

		conf.put("whois_enrichment_tag", "whois_enrichment");
		conf.put("host_regex", "host\":\"(.*?)\"");
		conf.put("enrichment_source_ip", "172.30.9.108:60000");

		// ------------ES BOLT configuration

		conf.put("es_ip", "172.30.9.148");
		conf.put("es_port", 9300);
		conf.put("es_cluster_name", "devo_es");
		conf.put("index_name", "sourcefire_index");
		conf.put("document_name", "sourcefire_doc");
		conf.put("es_bulk", 200);

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

		// builder.setBolt("ParserBolt",
		// new ParserBolt(new BasicSourcefireParser()), parallelism_hint)
		// .shuffleGrouping("EnrichmentSpout").setNumTasks(num_tasks);

//		builder.setBolt("GeoEnrichBolt",
//				new GeoEnrichmentBolt(new GeoMysqlAdapter()), parallelism_hint)
//				.shuffleGrouping("ParserBolt").setNumTasks(num_tasks);

		// builder.setBolt("WhoisEnrichmentBolt",
		// new WhoisEnrichmentBolt(new WhoisHBaseAdapter()),
		// parallelism_hint).shuffleGrouping("ParserBolt").setNumTasks(num_tasks);

//		builder.setBolt("IndexingBolt",
//				new IndexingBolt(new ESBaseBulkAdapter()), parallelism_hint)
//				.shuffleGrouping("GeoEnrichBolt").setNumTasks(num_tasks);

		// ------------HDFS BOLT configuration

//		FileNameFormat fileNameFormat = new DefaultFileNameFormat()
//				.withPath("/" + topology_name + "/");
//		RecordFormat format = new DelimitedRecordFormat()
//				.withFieldDelimiter("|");

//		SyncPolicy syncPolicy = new CountSyncPolicy(5);
//		FileRotationPolicy rotationPolicy = new FileSizeRotationPolicy(5.0f,
//				Units.KB);

//		HdfsBolt hdfsBolt = new HdfsBolt().withFsUrl(hdfs_path)
//				.withFileNameFormat(fileNameFormat).withRecordFormat(format)
//				.withRotationPolicy(rotationPolicy).withSyncPolicy(syncPolicy);

//		builder.setBolt("HDFSBolt", hdfsBolt, parallelism_hint)
//				.shuffleGrouping("EnrichmentSpout").setNumTasks(num_tasks);

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