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

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;

import com.opensoc.hbase.HBaseBolt;
import com.opensoc.hbase.HBaseStreamPartitioner;

import org.apache.storm.hbase.bolt.mapper.SimpleHBaseMapper;
import org.json.simple.JSONObject;

import storm.kafka.BrokerHosts;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.ZkHosts;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.Grouping;
import backtype.storm.spout.RawMultiScheme;
import backtype.storm.spout.RawScheme;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.BoltDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;

import com.opensoc.hbase.TupleTableConfig;
import com.opensoc.json.serialization.JSONKryoSerializer;
import com.opensoc.parsing.PcapParserBolt;
import com.opensoc.test.spouts.PcapSimulatorSpout;
import com.opensoc.topologyhelpers.SettingsLoader;

public class PCAPTopology {

	static Configuration config;
	static TopologyBuilder builder;
	static String component = "Spout";
	static Config conf;
	static String subdir = "pcap";

	public static void main(String[] args) throws Exception {

		String config_path = ".";
		boolean success = true;

		try {
			config_path = args[0];
		} catch (Exception e) {
			config_path = "OpenSOC_Configs";
		}

		String topology_conf_path = config_path + "/topologies/" + subdir
				+ "/topology.conf";
		String environment_identifier_path = config_path
				+ "/topologies/environment_identifier.conf";
		String topology_identifier_path = config_path + "/topologies/" + subdir
				+ "/topology_identifier.conf";

		System.out.println("[OpenSOC] Looking for environment identifier: "
				+ environment_identifier_path);
		System.out.println("[OpenSOC] Looking for topology identifier: "
				+ topology_identifier_path);
		System.out.println("[OpenSOC] Looking for topology config: "
				+ topology_conf_path);

		config = new PropertiesConfiguration(topology_conf_path);

		JSONObject environment_identifier = SettingsLoader
				.loadEnvironmentIdnetifier(environment_identifier_path);
		JSONObject topology_identifier = SettingsLoader
				.loadTopologyIdnetifier(topology_identifier_path);

		String topology_name = SettingsLoader.generateTopologyName(
				environment_identifier, topology_identifier);

		System.out.println("[OpenSOC] Initializing Topology: " + topology_name);

		builder = new TopologyBuilder();

		conf = new Config();
		conf.registerSerialization(JSONObject.class, JSONKryoSerializer.class);
		conf.setDebug(config.getBoolean("debug.mode"));

		if (config.getBoolean("spout.generator.enabled", false)) {
			String component_name = config.getString("spout.generator.name",
					"DefaultTopologySpout");
			success = initializeSimulatorSpout("SampleInput/PcapExampleOutput",
					component_name);
			component = component_name;

			System.out.println("[OpenSOC] Component " + component
					+ " initialized");
		}

		if (config.getBoolean("spout.kafka.enabled", true)) {

			String component_name = config.getString("spout.kafka.name",
					"DefaultKafkaSpout");
			success = initializeKafkaSpout(component_name);
			component = component_name;

			System.out.println("[OpenSOC] Component " + component_name
					+ " initialized");

		}

		if (config.getBoolean("bolt.parser.enabled", true)) {

			String component_name = config.getString("bolt.parser.name",
					"DefaultParserBolt");
			success = initializeParserBolt(component_name);
			component = component_name;

			System.out.println("[OpenSOC] Component " + component_name
					+ " initialized");

		}

		if (config.getBoolean("bolt.hdfs.enabled", true)) {

			String component_name = config.getString("bolt.hdfs.name",
					"DefaultHDFSBolt");
			
			String shuffleType = config.getString("bolt.hdfs.shuffle.type",
					"direct");
			success = initializeHdfsBolt(component_name, shuffleType);

			System.out.println("[OpenSOC] Component " + component_name
					+ " initialized");

		}

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

	public static boolean initializeSimulatorSpout(String file_path, String name) {

		try {
			builder.setSpout(name, new PcapSimulatorSpout(),
					config.getInt("spout.generator.parallelism.hint"))
					.setNumTasks(config.getInt("spout.generator.num.tasks"));

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		return true;
	}

	public static boolean initializeKafkaSpout(String name) {
		try {

			BrokerHosts zk = new ZkHosts(config.getString("kafka.zk"));
			String input_topic = config.getString("spout.kafka.topic");
			String instance_id = config.getString("instance.id");
			String zk_root = config.getString("spout.kafka.zk.root");

			SpoutConfig kafkaConfig = new SpoutConfig(zk, input_topic, zk_root,
					instance_id);
			kafkaConfig.scheme = new SchemeAsMultiScheme(new RawScheme());
			kafkaConfig.forceFromStart = config.getBoolean(
					"kafka.spout.forcefromstart", false);
			kafkaConfig.startOffsetTime = config
					.getLong("spout.kafka.start.offset.time");

			kafkaConfig.bufferSizeBytes = config
					.getInt("spout.kafka.buffer.size.bytes");
			kafkaConfig.fetchSizeBytes = config
					.getInt("spout.kafka.fetch.size.bytes");
			kafkaConfig.socketTimeoutMs = config
					.getInt("spout.kafka.socket.timeout.ms");

			kafkaConfig.scheme = new RawMultiScheme();
			
			kafkaConfig.forceFromStart = true;

			builder.setSpout(name, new KafkaSpout(kafkaConfig),
					config.getInt("spout.kafka.parallelism.hint")).setNumTasks(
					config.getInt("spout.kafka.num.tasks"));

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		return true;
	}

	public static boolean initializeParserBolt(String name) {
		try {
			builder.setBolt(name, new PcapParserBolt(),
					config.getInt("bolt.parser.parallelism.hint"))
					.setNumTasks(config.getInt("bolt.parser.num.tasks"))
					.shuffleGrouping(component);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		return true;
	}

	public static boolean initializeHdfsBolt(String name, String shuffleType) {

		try {

			String tableName = config.getString(
					"bolt.hbase.table.name").toString();
			TupleTableConfig hbaseBoltConfig = new TupleTableConfig(
					tableName,
					config.getString(
							"bolt.hbase.table.key.tuple.field.name")
							.toString(),
							config.getString(
							"bolt.hbase.table.key.tuple.field.name")
							.toString());

			String allColumnFamiliesColumnQualifiers = config.getString(
					"bolt.hbase.table.fields").toString();
			// This is expected in the form
			// "<cf1>:<cq11>,<cq12>,<cq13>|<cf2>:<cq21>,<cq22>|......."
			String[] tokenizedColumnFamiliesWithColumnQualifiers = StringUtils
					.split(allColumnFamiliesColumnQualifiers, "\\|");
			for (String tokenizedColumnFamilyWithColumnQualifiers : tokenizedColumnFamiliesWithColumnQualifiers) {
				String[] cfCqTokens = StringUtils.split(
						tokenizedColumnFamilyWithColumnQualifiers, ":");
				String columnFamily = cfCqTokens[0];
				String[] columnQualifiers = StringUtils.split(cfCqTokens[1],
						",");
				for (String columnQualifier : columnQualifiers) {
					hbaseBoltConfig.addColumn(columnFamily, columnQualifier);
				}

				//hbaseBoltConfig.setDurability(Durability.valueOf(conf.get( "storm.topology.pcap.bolt.hbase.durability").toString()));
			    
				
				hbaseBoltConfig.setBatch(Boolean.valueOf(config.getString("bolt.hbase.enable.batching").toString()));
				
				 BoltDeclarer declarer = builder.setBolt(name, new HBaseBolt(hbaseBoltConfig), config.getInt("bolt.hbase.parallelism.hint")).setNumTasks(
							config.getInt("bolt.hbase.num.tasks")); 
				 
				if (Grouping._Fields.CUSTOM_OBJECT.toString().equalsIgnoreCase(shuffleType)) {
			        declarer.customGrouping(
			            component,
			            "pcap_data_stream",
			            new HBaseStreamPartitioner(hbaseBoltConfig.getTableName(), 0, Integer.parseInt(conf.get(
			                "bolt.hbase.partitioner.region.info.refresh.interval.mins").toString())));
			      } else if (Grouping._Fields.DIRECT.toString().equalsIgnoreCase(shuffleType)) {
			        declarer.fieldsGrouping(component, "pcap_data_stream", new Fields("region"));
			      }
				
				

				

			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return true;
	}
}
