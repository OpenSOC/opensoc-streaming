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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;

import storm.kafka.BrokerHosts;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;

import com.opensoc.enrichment.adapters.geo.GeoMysqlAdapter;
import com.opensoc.enrichment.common.GenericEnrichmentBolt;
import com.opensoc.parsing.TelemetryParserBolt;
import com.opensoc.parsing.parsers.BasicSourcefireParser;
import com.opensoc.test.spouts.SourcefireTestSpout;

/**
 * This is a basic example of a Storm topology.
 */
public class SourcefireEnrichmentTestTopology {

	public static void main(String[] args) throws Exception {

		Configuration config = new PropertiesConfiguration(
				"/Users/jsirota/Documents/github-opensoc-streaming/opensoc-streaming/OpenSOC-Topologies/src/main/resources/TopologyConfigs/sourcefire.conf");

		String topology_name = config.getString("topology.name");

		TopologyBuilder builder = new TopologyBuilder();

		boolean localMode = true;
		String hdfs_path = "hdfs://172.30.9.110:8020";

		// /--------TODO: what should this be set to?
		BrokerHosts zk_broker_hosts = null;
		String zkRoot = "?";

		Config conf = new Config();
		conf.setDebug(config.getBoolean("debug.mode"));

		builder.setSpout("EnrichmentSpout", new SourcefireTestSpout(),
				config.getInt("spout.test.parallelism.hint")).setNumTasks(
				config.getInt("spout.test.num.tasks"));

		// ------------Parser Bolt Configuration

		TelemetryParserBolt parser_bolt = new TelemetryParserBolt()
				.withMessageParser(new BasicSourcefireParser())
				.withOutputFieldName(topology_name);

		builder.setBolt("ParserBolt", parser_bolt,
				config.getInt("bolt.parser.parallelism.hint"))
				.shuffleGrouping("EnrichmentSpout")
				.setNumTasks(config.getInt("bolt.parser.num.tasks"));

		// ------------Geo Enrichment Bolt Configuration

		Map<String, Pattern> patterns = new HashMap<String, Pattern>();
		patterns.put("originator_ip_regex", Pattern.compile(config
				.getString("bolt.enrichment.geo.originator_ip_regex")));
		patterns.put("responder_ip_regex", Pattern.compile(config
				.getString("bolt.enrichment.geo.responder_ip_regex")));

		GeoMysqlAdapter geo_adapter = new GeoMysqlAdapter(
				config.getString("bolt.enrichment.geo.adapter.ip"),
				config.getInt("bolt.enrichment.geo.adapter.port"),
				config.getString("bolt.enrichment.geo.adapter.username"),
				config.getString("bolt.enrichment.geo.adapter.password"),
				config.getString("bolt.enrichment.geo.adapter.table"));

		GenericEnrichmentBolt geo_enrichment = new GenericEnrichmentBolt()
				.withEnrichmentTag(
						config.getString("bolt.enrichment.geo.geo_enrichment_tag"))
				.withOutputFieldName(topology_name)
				.withAdapter(geo_adapter)
				.withMaxTimeRetain(
						config.getInt("bolt.enrichment.geo.MAX_TIME_RETAIN"))
				.withMaxCacheSize(
						config.getInt("bolt.enrichment.geo.MAX_CACHE_SIZE"))
				.withPatterns(patterns);

		builder.setBolt("GeoEnrichBolt", geo_enrichment,
				config.getInt("bolt.enrichment.geo.parallelism.hint"))
				.shuffleGrouping("ParserBolt")
				.setNumTasks(config.getInt("bolt.enrichment.geo.num.tasks"));

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

		if (config.getBoolean("local.mode")) {
			conf.setNumWorkers(config.getInt("num.workers"));
			conf.setMaxTaskParallelism(1);
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology(topology_name, conf,
					builder.createTopology());
		} else {

			conf.setNumWorkers(config.getInt("num.workers"));
			StormSubmitter.submitTopology(topology_name, conf,
					builder.createTopology());

		}

	}
}