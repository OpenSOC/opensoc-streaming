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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.json.simple.JSONObject;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;

import com.opensoc.enrichment.adapters.geo.GeoMysqlAdapter;
import com.opensoc.enrichment.adapters.whois.WhoisHBaseAdapter;
import com.opensoc.enrichment.common.EnrichmentAdapter;
import com.opensoc.enrichment.common.GenericEnrichmentBolt;
import com.opensoc.indexing.TelemetryIndexingBolt;
import com.opensoc.indexing.adapters.ESBaseBulkAdapter;
import com.opensoc.parsing.TelemetryParserBolt;
import com.opensoc.parsing.parsers.BasicSourcefireParser;
import com.opensoc.tagger.interfaces.TaggerAdapter;
import com.opensoc.tagging.TelemetryTaggerBolt;
import com.opensoc.tagging.adapters.StaticAllTagger;
import com.opensoc.test.spouts.GenericInternalTestSpout;

/**
 * This is a basic example of a Storm topology.
 */
public class SourcefireEnrichmentTestTopology {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {

		String config_path = "";

		try {
			config_path = args[0];
		} catch (Exception e) {
			config_path = "TopologyConfigs/sourcefire.conf";
		}

		Configuration config = new PropertiesConfiguration(config_path);

		String topology_name = config.getString("topology.name");

		TopologyBuilder builder = new TopologyBuilder();

		Config conf = new Config();
		conf.setDebug(config.getBoolean("debug.mode"));

		GenericInternalTestSpout testSpout = new GenericInternalTestSpout()
				.withFilename("SampleInput/SourcefireExampleOutput")
				.withRepeating(false);

		builder.setSpout("EnrichmentSpout", testSpout,
				config.getInt("spout.test.parallelism.hint")).setNumTasks(
				config.getInt("spout.test.num.tasks"));

		// ------------Parser Bolt Configuration

		TelemetryParserBolt parser_bolt = new TelemetryParserBolt()
				.withMetricProperties(
						config.getProperties("com.opensoc.metrics.TelemetryParserBolt"))
				.withMessageParser(new BasicSourcefireParser())
				.withOutputFieldName(topology_name);

		builder.setBolt("ParserBolt", parser_bolt,
				config.getInt("bolt.parser.parallelism.hint"))
				.shuffleGrouping("EnrichmentSpout")
				.setNumTasks(config.getInt("bolt.parser.num.tasks"));

		// -------------Alerts Bolt

		JSONObject static_alerts_message = new JSONObject();
		static_alerts_message.put("source",
				config.getString("bolt.alerts.staticsource"));
		static_alerts_message.put("priority",
				config.getString("bolt.alerts.staticpriority"));
		static_alerts_message.put("designated_host", "NOT CONFIGURED");

		TaggerAdapter tagger = new StaticAllTagger(static_alerts_message);

		TelemetryTaggerBolt alerts_bolt = new TelemetryTaggerBolt()
				.withMessageTagger(tagger).withOutputFieldName(topology_name);

		builder.setBolt("AlertsBolt", alerts_bolt,
				config.getInt("bolt.alerts.parallelism.hint"))
				.shuffleGrouping("ParserBolt")
				.setNumTasks(config.getInt("bolt.alerts.num.tasks"));

		
		 // ------------Geo Enrichment Bolt Configuration
		 
		 List<String> geo_keys = new ArrayList<String>();
		  geo_keys.add(config.getString("bolt.enrichment.geo.source_ip"));
		  geo_keys.add(config.getString("bolt.enrichment.geo.resp_ip"));
		  
		  GeoMysqlAdapter geo_adapter = new GeoMysqlAdapter(
		  config.getString("bolt.enrichment.geo.adapter.ip"),
		  config.getInt("bolt.enrichment.geo.adapter.port"),
		  config.getString("bolt.enrichment.geo.adapter.username"),
		  config.getString("bolt.enrichment.geo.adapter.password"),
		  config.getString("bolt.enrichment.geo.adapter.table"));
		  
		  GenericEnrichmentBolt geo_enrichment = new GenericEnrichmentBolt()
		  .withEnrichmentTag(
		  config.getString("bolt.enrichment.geo.geo_enrichment_tag"))
		  .withOutputFieldName(topology_name) .withAdapter(geo_adapter)
		  .withMaxTimeRetain(
		  config.getInt("bolt.enrichment.geo.MAX_TIME_RETAIN"))
		  .withMaxCacheSize(
		  config.getInt("bolt.enrichment.geo.MAX_CACHE_SIZE"))
		  .withKeys(geo_keys).withMetricProperties(
					config.getProperties("com.opensoc.metrics.GenericEnrichmentBolt"));
		  
		  builder.setBolt("GeoEnrichBolt", geo_enrichment,
		  config.getInt("bolt.enrichment.geo.parallelism.hint"))
		  .shuffleGrouping("AlertsBolt")
		  .setNumTasks(config.getInt("bolt.enrichment.geo.num.tasks"));
		  
		  // ------------Indexing BOLT configuration
		  
		  TelemetryIndexingBolt indexing_bolt = new TelemetryIndexingBolt()
		  .withIndexIP(config.getString("bolt.indexing.indexIP"))
		  .withIndexPort(config.getInt("bolt.indexing.port"))
		  .withClusterName(config.getString("bolt.indexing.clustername"))
		  .withIndexName(config.getString("bolt.indexing.indexname"))
		  .withDocumentName( config.getString("bolt.indexing.documentname"))
		  .withBulk(config.getInt("bolt.indexing.bulk"))
		  .withOutputFieldName(topology_name) .withIndexAdapter(new
		  ESBaseBulkAdapter()).withMetricProperties(
					config.getProperties("com.opensoc.metrics.TelemetryIndexingBolt"));
		  
		  builder.setBolt("IndexingBolt", indexing_bolt,
		  config.getInt("bolt.indexing.parallelism.hint"))
		  .shuffleGrouping("GeoEnrichBolt")
		  .setNumTasks(config.getInt("bolt.indexing.num.tasks"));
		 
		// ------------HDFS BOLT configuration
		/*
		 * FileNameFormat fileNameFormat = new DefaultFileNameFormat()
		 * .withPath("/" + topology_name + "/"); RecordFormat format = new
		 * DelimitedRecordFormat() .withFieldDelimiter("|");
		 * 
		 * SyncPolicy syncPolicy = new CountSyncPolicy(
		 * config.getInt("bolt.hdfs.size.sink.policy")); FileRotationPolicy
		 * rotationPolicy = new FileSizeRotationPolicy(
		 * config.getLong("bolt.hdfs.size.rotation.policy"), Units.KB);
		 * 
		 * HdfsBolt hdfsBolt = new HdfsBolt().withFsUrl("hdfs://" +
		 * config.getString("bolt.hdfs.IP"))
		 * .withFileNameFormat(fileNameFormat).withRecordFormat(format)
		 * .withRotationPolicy(rotationPolicy).withSyncPolicy(syncPolicy);
		 * 
		 * 
		 * 
		 * builder.setBolt("HDFSBolt", hdfsBolt,
		 * config.getInt("bolt.hdfs.parallelism.hint"))
		 * .shuffleGrouping("ParserBolt")
		 * .setNumTasks(config.getInt("bolt.hdfs.num.tasks"));
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