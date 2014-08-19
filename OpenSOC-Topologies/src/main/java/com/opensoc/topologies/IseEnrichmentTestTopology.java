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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import storm.kafka.bolt.KafkaBolt;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;

import com.opensoc.enrichment.adapters.cif.CIFHbaseAdapter;
import com.opensoc.enrichment.adapters.geo.GeoMysqlAdapter;
import com.opensoc.enrichment.adapters.whois.WhoisHBaseAdapter;
import com.opensoc.enrichment.common.EnrichmentAdapter;
import com.opensoc.enrichment.common.GenericEnrichmentBolt;
import com.opensoc.enrichment.host.HostAdapter;
import com.opensoc.indexing.TelemetryIndexingBolt;
import com.opensoc.indexing.adapters.ESBaseBulkAdapter;
import com.opensoc.json.serialization.JSONKryoSerializer;
import com.opensoc.parsing.AbstractParserBolt;
import com.opensoc.parsing.TelemetryParserBolt;
import com.opensoc.parsing.parsers.BasicBroParser;
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
			config_path = "TopologyConfigs/bro.conf";
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
