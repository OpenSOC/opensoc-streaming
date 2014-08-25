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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.storm.hdfs.bolt.HdfsBolt;
import org.apache.storm.hdfs.bolt.format.*;
import org.apache.storm.hdfs.bolt.rotation.FileRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy.Units;
import org.apache.storm.hdfs.bolt.sync.CountSyncPolicy;
import org.apache.storm.hdfs.bolt.sync.SyncPolicy;
import org.json.simple.JSONObject;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;

import com.opensoc.enrichment.adapters.cif.CIFHbaseAdapter;
import com.opensoc.enrichment.adapters.whois.WhoisHBaseAdapter;
import com.opensoc.enrichment.common.EnrichmentAdapter;
import com.opensoc.enrichment.common.GenericEnrichmentBolt;
import com.opensoc.indexing.TelemetryIndexingBolt;
import com.opensoc.indexing.adapters.ESBaseBulkAdapter;
import com.opensoc.json.serialization.JSONKryoSerializer;
import com.opensoc.parsing.AbstractParserBolt;
import com.opensoc.parsing.TelemetryParserBolt;
import com.opensoc.parsing.parsers.BasicIseParser;
import com.opensoc.test.spouts.GenericInternalTestSpout;

/**
 * This is a basic example of a Storm topology.
 */

public class IseEnrichmentTestTopology {

	public static void main(String[] args) throws Exception {

		String config_path = "";

		try {
			config_path = args[0];
		} catch (Exception e) {
			config_path = "TopologyConfigs/ise.conf";
		}

		Configuration config = new PropertiesConfiguration(config_path);

		String topology_name = config.getString("topology.name");

		TopologyBuilder builder = new TopologyBuilder();

		Config conf = new Config();
		conf.registerSerialization(JSONObject.class, JSONKryoSerializer.class);
		conf.setDebug(config.getBoolean("debug.mode"));

		// Testing Spout
		
		  GenericInternalTestSpout testSpout = new GenericInternalTestSpout()
		  .withFilename("SampleInput/ISESampleOutput").withRepeating(false);
		  
		  builder.setSpout("EnrichmentSpout", testSpout,
		  config.getInt("spout.test.parallelism.hint")).setNumTasks(
		  config.getInt("spout.test.num.tasks"));
		 

		// ------------ParserBolt configuration

		AbstractParserBolt parser_bolt = new TelemetryParserBolt()
				.withMessageParser(new BasicIseParser()).withOutputFieldName(
						topology_name).withMetricConfig(config);
						

		builder.setBolt("ParserBolt", parser_bolt,
				config.getInt("bolt.parser.parallelism.hint"))
				.shuffleGrouping("EnrichmentSpout")
				.setNumTasks(config.getInt("bolt.parser.num.tasks"));
		
				// ------------CIF bolt configuration
				// Add all CIF json keys that need are used for CIF enhancement.

				List<String> cif_keys = new ArrayList<String>();

				cif_keys.add(config.getString("bolt.enrichment.cif.source_ip"));
				cif_keys.add(config.getString("bolt.enrichment.cif.resp_ip"));
				cif_keys.add(config.getString("bolt.enrichment.cif.host"));
				cif_keys.add(config.getString("bolt.enrichment.cif.email"));

				GenericEnrichmentBolt cif_enrichment = new GenericEnrichmentBolt()
				.withEnrichmentTag(
						config.getString("bolt.enrichment.cif.enrichment_tag"))
						.withAdapter(
								new CIFHbaseAdapter(config.getString("kafka.zk.list"),
										config.getString("kafka.zk.port"),config.getString("bolt.enrichment.cif.tablename")))
						.withOutputFieldName(topology_name)
						.withEnrichmentTag("CIF_Enrichment")
						.withKeys(cif_keys)
						.withMaxTimeRetain(
								config.getInt("bolt.enrichment.cif.MAX_TIME_RETAIN"))
						.withMaxCacheSize(
								config.getInt("bolt.enrichment.cif.MAX_CACHE_SIZE"))
								.withMetricConfiguration(config);

				builder.setBolt("CIFEnrichmentBolt", cif_enrichment,
						config.getInt("bolt.enrichment.cif.parallelism.hint"))
						.shuffleGrouping("ParserBolt")
						.setNumTasks(config.getInt("bolt.enrichment.cif.num.tasks"));
				
				
				// ------------Indexing BOLT configuration

				TelemetryIndexingBolt indexing_bolt = new TelemetryIndexingBolt()
						.withIndexIP(config.getString("bolt.indexing.indexIP"))
						.withIndexPort(config.getInt("bolt.indexing.port"))
						.withClusterName(config.getString("bolt.indexing.clustername"))
						.withIndexName(config.getString("bolt.indexing.indexname"))
						.withDocumentName(
								config.getString("bolt.indexing.documentname"))
						.withBulk(config.getInt("bolt.indexing.bulk"))
						.withOutputFieldName(topology_name)
						.withIndexAdapter(new ESBaseBulkAdapter())
						.withMetricConfiguration(config)
						;

				builder.setBolt("IndexingBolt", indexing_bolt,
						config.getInt("bolt.indexing.parallelism.hint"))
						.shuffleGrouping("CIFEnrichmentBolt")
						.setNumTasks(config.getInt("bolt.indexing.num.tasks"));
				
				
				
				// * ------------HDFS BOLT For Enriched Data configuration

					FileNameFormat fileNameFormat_enriched = new DefaultFileNameFormat()
							.withPath("/" + topology_name + "_enriched/");
					RecordFormat format_enriched = new DelimitedRecordFormat()
							.withFieldDelimiter("|");

					SyncPolicy syncPolicy_enriched = new CountSyncPolicy(5);
					FileRotationPolicy rotationPolicy_enriched = new FileSizeRotationPolicy(config.getFloat("bolt.hdfs.size.rotation.policy"),
							Units.KB);

					HdfsBolt hdfsBolt_enriched = new HdfsBolt().withFsUrl(config.getString("bolt.hdfs.fs.url"))
							.withFileNameFormat(fileNameFormat_enriched).withRecordFormat(format_enriched)
							.withRotationPolicy(rotationPolicy_enriched).withSyncPolicy(syncPolicy_enriched);

					builder.setBolt("HDFSBolt_enriched", hdfsBolt_enriched, config.getInt("bolt.hdfs.parallelism.hint"))
							.shuffleGrouping("CIFEnrichmentBolt").setNumTasks(config.getInt("bolt.hdfs.num.tasks"));
				
		

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
