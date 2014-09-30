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


/**
 * This is a basic example of a Storm topology.
 */

/*
public class StressTestES {

	public static void main(String[] args) throws Exception {
		TopologyBuilder builder = new TopologyBuilder();

		int parallelism_hint = 10;
		int num_tasks = 20;
		int num_workers = 20;

		String topology_name = "StressTestES";
		int localMode = 0;

		// /--------TODO: what should this be set to?
		BrokerHosts zk_broker_hosts = null;
		String zkRoot = "?";

		Config conf = new Config();
		conf.setDebug(true);

		// ------------ES BOLT configuration

		conf.put("es_ip", "172.30.9.148");
		conf.put("es_port", 9300);
		conf.put("es_cluster_name", "devo_es");
		conf.put("index_name", "sourcefire_index");
		conf.put("document_name", "sourcefire_doc");
		conf.put("es_bulk", 1000);



		builder.setSpout("EnrichmentSpout", new ESLoadingSpout(),
				parallelism_hint).setNumTasks(num_tasks);

/*		builder.setBolt("ParserBolt",
				new ParserBolt(new BasicSourcefireParser()), parallelism_hint)
				.shuffleGrouping("EnrichmentSpout").setNumTasks(num_tasks);

		builder.setBolt("IndexingBolt",
				new TelemetryIndexingBolt(new ESBaseBulkAdapter()), parallelism_hint)
				.shuffleGrouping("GeoEnrichBolt").setNumTasks(num_tasks);

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
}*/