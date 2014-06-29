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

import storm.kafka.BrokerHosts;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;

import backtype.storm.spout.SchemeAsMultiScheme;

/**
 * This is a basic example of a Storm topology.
 */

public class BroEnrichmentTestTopology {

	public static void main(String[] args) throws Exception {
		TopologyBuilder builder = new TopologyBuilder();

		// ------------KAFKA spout configuration

		BrokerHosts zk = new ZkHosts("172.30.9.115:2181");

		SpoutConfig kafkaConfig = new SpoutConfig(zk, "metadata", "/", "bro");

		kafkaConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
		kafkaConfig.forceFromStart = Boolean.valueOf("True");
		kafkaConfig.startOffsetTime = -1;

		builder.setSpout("kafka-spout", new KafkaSpout(kafkaConfig), 1)
				.setNumTasks(1);

		/*
		 * builder.setSpout("EnrichmentSpout", new BroTestSpout(), 1); //
		 * builder.setBolt("ParserBolt", new ParserBolt(new BasicBroParser()),
		 * 1).shuffleGrouping("EnrichmentSpout");
		 * builder.setBolt("GeoEnrichBolt", new GeoEnrichmentBolt(new
		 * GeoMysqlAdapter()), 1).shuffleGrouping("ParserBolt");
		 * //builder.setBolt("WhoisEnrichBolt", new WhoisEnrichmentBolt(new
		 * HBaseAdapter()), 1).shuffleGrouping("GeoEnrichBolt");
		 * builder.setBolt("IndexingBolt", new TelemetryIndexingBolt(new
		 * ESBulkRotatingAdapter()), 1).shuffleGrouping("GeoEnrichBolt");
		 * builder.setBolt("PrintgBolt", new PrintingBolt(),
		 * 1).shuffleGrouping("GeoEnrichBolt");
		
		*/
		
		  Config conf = new Config(); 
		  conf.setDebug(true);
		  
		
		 
		if (args != null && args.length > 0) {
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
