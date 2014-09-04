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
import org.json.simple.JSONObject;

import storm.kafka.BrokerHosts;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.ZkHosts;
import backtype.storm.Config;
import backtype.storm.spout.RawScheme;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;

import com.opensoc.json.serialization.JSONKryoSerializer;
import com.opensoc.topologyhelpers.SettingsLoader;

public class PCAPTopology {

	static Configuration config;
	static TopologyBuilder builder;
	static String component = "Spout";
	static Config conf;
	static String subdir = "lancope";

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
			success = initializeGeneratorSpout("SampleInput/PcapExampleOutput",
					component_name);
			component = component_name;

			System.out.println("[OpenSOC] Component " + component
					+ " initialized");
		}

		if (config.getBoolean("spout.kafka.enabled", true)) {

			String component_name = config.getString("spout.kafka.name",
					"KafkaSpout");
			success = initializeKafkaSpout(component_name);
			component = component_name;
			
			System.out.println("[OpenSOC] Component " + component
					+ " initialized");

		}

	}

	public static boolean initializeGeneratorSpout(String file_path, String name) {

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

			// kafkaConfig.scheme = new RawMultiScheme();

			builder.setSpout(name, new KafkaSpout(kafkaConfig),
					config.getInt("spout.kafka.parallelism.hint")).setNumTasks(
					config.getInt("spout.kafka.num.tasks"));

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		return true;
	}

}
