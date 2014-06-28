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

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;

import com.opensoc.enrichments.geo.GeoEnrichmentBolt;
import com.opensoc.enrichments.geo.adapters.GeoMysqlAdapter;
import com.opensoc.indexing.IndexingBolt;
import com.opensoc.indexing.adapters.ESBulkRotatingAdapter;
import com.opensoc.parsing.parsers.BasicBroParser;
import com.opensoc.test.bolts.PrintingBolt;
import com.opensoc.test.spouts.BroTestSpout;

/**
 * This is a basic example of a Storm topology.
 */
public class BroEnrichmentTestTopology 
{

  public static void main(String[] args) throws Exception {
    TopologyBuilder builder = new TopologyBuilder();

    builder.setSpout("EnrichmentSpout", new BroTestSpout(), 1);
//    builder.setBolt("ParserBolt", new ParserBolt(new BasicBroParser()), 1).shuffleGrouping("EnrichmentSpout");
    builder.setBolt("GeoEnrichBolt", new GeoEnrichmentBolt(new GeoMysqlAdapter()), 1).shuffleGrouping("ParserBolt");
    //builder.setBolt("WhoisEnrichBolt", new WhoisEnrichmentBolt(new HBaseAdapter()), 1).shuffleGrouping("GeoEnrichBolt");
    builder.setBolt("IndexingBolt", new IndexingBolt(new ESBulkRotatingAdapter()),
			1).shuffleGrouping("GeoEnrichBolt");
    builder.setBolt("PrintgBolt", new PrintingBolt(), 1).shuffleGrouping("GeoEnrichBolt");
    
    Config conf = new Config();
    conf.setDebug(true);
    
    conf.put("MAX_CACHE_SIZE", 10000);
    conf.put("MAX_TIME_RETAIN", 10);
    
    conf.put("geo_enrichment_source_ip", "172.30.9.54");
    conf.put("originator_ip_regex", "id.orig_h\":\"(.*?)\"");
    conf.put("responder_ip_regex", "id.resp_h\":\"(.*?)\"");
    conf.put("geo_enrichment_tag", "geo_enrichment");
    
    conf.put("whois_enrichment_tag", "whois_enrichment");
    conf.put("host_regex", "host\":\"(.*?)\"");
    conf.put("enrichment_source_ip",  "172.30.9.108:60000");

    
	conf.put("es_ip", "172.30.9.148");
	conf.put("es_port", 9300);
	conf.put("es_cluster_name", "devo_es");
	conf.put("index_name", "bro_index");
	conf.put("document_name", "bro_doc");
	conf.put("es_bulk", 50);
	

    if (args != null && args.length > 0) {
      conf.setNumWorkers(1);

      StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
    }
    else {

      LocalCluster cluster = new LocalCluster();
      cluster.submitTopology("test", conf, builder.createTopology());
     // Utils.sleep(10000);
      //cluster.killTopology("test");
      //cluster.shutdown();
    }
  }
}