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
import org.json.simple.JSONObject;

import storm.kafka.BrokerHosts;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;
import storm.kafka.bolt.KafkaBolt;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.spout.SchemeAsMultiScheme;
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
import com.opensoc.parsing.parsers.BasicBroParser;

/**
 * This is a basic example of a Storm topology.
 */

public class BroEnrichmentTestTopology {

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

		// ------------KAFKA spout configuration

		BrokerHosts zk = new ZkHosts(config.getString("kafka.zk"));
		String input_topic = config.getString("spout.kafka.topic");
		SpoutConfig kafkaConfig = new SpoutConfig(zk, input_topic, "",
				input_topic);
		kafkaConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
		// kafkaConfig.forceFromStart = Boolean.valueOf("True");
		kafkaConfig.startOffsetTime = -1;

		builder.setSpout("kafka-spout", new KafkaSpout(kafkaConfig),
				config.getInt("spout.kafka.parallelism.hint")).setNumTasks(
				config.getInt("spout.kafka.num.tasks"));


		// Testing Spout
		/*
		  GenericInternalTestSpout testSpout = new GenericInternalTestSpout()
		  .withFilename("SampleInput/BroExampleOutput").withRepeating( false);
		  
		  builder.setSpout("EnrichmentSpout", testSpout,
		  config.getInt("spout.test.parallelism.hint")).setNumTasks(
		  config.getInt("spout.test.num.tasks"));
		 */

		// ------------ParserBolt configuration

		AbstractParserBolt parser_bolt = new TelemetryParserBolt()
				.withMessageParser(new BasicBroParser()).withOutputFieldName(
						topology_name)
				.withMetricProperties(config.getProperties("com.opensoc.metrics.TelemetryParserBolt"));
						

		builder.setBolt("ParserBolt", parser_bolt,
				config.getInt("bolt.parser.parallelism.hint"))
				.shuffleGrouping("kafka-spout")
				.setNumTasks(config.getInt("bolt.parser.num.tasks"));
		
		
		// ------------Whois Enrichment Bolt Configuration

		List<String> whois_keys = new ArrayList<String>();
		String[] keys_from_settings = config.getString("bolt.enrichment.whois.source").split(",");
		
		for(String key : keys_from_settings)
			whois_keys.add(key);

		EnrichmentAdapter whois_adapter = new WhoisHBaseAdapter(
				config.getString("bolt.enrichment.whois.hbase.table.name"),
				config.getString("kafka.zk.list"),
				config.getString("kafka.zk.port"));

		GenericEnrichmentBolt whois_enrichment = new GenericEnrichmentBolt()
				.withEnrichmentTag(
						config.getString("bolt.enrichment.whois.whois_enrichment_tag"))
				.withOutputFieldName(topology_name)
				.withAdapter(whois_adapter)
				.withMaxTimeRetain(
						config.getInt("bolt.enrichment.whois.MAX_TIME_RETAIN"))
				.withMaxCacheSize(
						config.getInt("bolt.enrichment.whois.MAX_CACHE_SIZE")).withKeys(whois_keys);

		builder.setBolt("WhoisEnrichBolt", whois_enrichment,
				config.getInt("bolt.enrichment.whois.parallelism.hint"))
				.shuffleGrouping("ParserBolt")
				.setNumTasks(config.getInt("bolt.enrichment.whois.num.tasks"));
		

		// ------------CIF bolt configuration

		/*
		 * Map<String, Pattern> cif_patterns = new HashMap<String, Pattern>();
		 * cif_patterns.put("source_ip", Pattern.compile(config
		 * .getString("bolt.enrichment.cif.source_ip")));
		 * cif_patterns.put("resp_ip", Pattern.compile(config
		 * .getString("bolt.enrichment.cif.resp_ip"))); cif_patterns.put("host",
		 * Pattern.compile(config.getString("bolt.enrichment.cif.host")));
		 * cif_patterns.put("email",
		 * Pattern.compile(config.getString("bolt.enrichment.cif.email")));
		 */

		// Add all CIF json keys that need are used for CIF enhancement.

		List<String> cif_keys = new ArrayList<String>();

		cif_keys.add(config.getString("bolt.enrichment.cif.source_ip"));
		cif_keys.add(config.getString("bolt.enrichment.cif.resp_ip"));
		cif_keys.add(config.getString("bolt.enrichment.cif.host"));
		cif_keys.add(config.getString("bolt.enrichment.cif.email"));

		GenericEnrichmentBolt cif_enrichment = new GenericEnrichmentBolt()
				.withAdapter(
						new CIFHbaseAdapter(config.getString("kafka.zk.list"),
								config.getString("kafka.zk.port"),config.getString("bolt.enrichment.cif.tablename")))
				.withOutputFieldName(topology_name)
				.withEnrichmentTag("CIF_Enrichment")
				.withKeys(cif_keys)
				.withMaxTimeRetain(
						config.getInt("bolt.enrichment.cif.MAX_TIME_RETAIN"))
				.withMaxCacheSize(
						config.getInt("bolt.enrichment.cif.MAX_CACHE_SIZE"));

		builder.setBolt("CIFEnrichmentBolt", cif_enrichment,
				config.getInt("bolt.enrichment.cif.parallelism.hint"))
				.shuffleGrouping("ParserBolt")
				.setNumTasks(config.getInt("bolt.enrichment.cif.num.tasks"));

		// ------------Kafka Bolt Configuration

		Map<String, String> kafka_broker_properties = new HashMap<String, String>();
		kafka_broker_properties.put("zk.connect", config.getString("kafka.zk"));
		kafka_broker_properties.put("metadata.broker.list",
				config.getString("kafka.br"));
		
		kafka_broker_properties.put("serializer.class",
				"com.opensoc.json.serialization.JSONKafkaSerializer");
		
		String output_topic = config.getString("bolt.kafka.topic");

		conf.put("kafka.broker.properties", kafka_broker_properties);
		conf.put("topic", output_topic);

		builder.setBolt("KafkaBolt", new KafkaBolt<String, String>(),
				config.getInt("bolt.kafka.parallelism.hint"))
				.shuffleGrouping("CIFEnrichmentBolt")
				.setNumTasks(config.getInt("bolt.kafka.num.tasks"));

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
				.withIndexAdapter(new ESBaseBulkAdapter());

		builder.setBolt("IndexingBolt", indexing_bolt,
				config.getInt("bolt.indexing.parallelism.hint"))
				.shuffleGrouping("CIFEnrichmentBolt")
				.setNumTasks(config.getInt("bolt.indexing.num.tasks"));


		// * ------------HDFS BOLT configuration

		FileNameFormat fileNameFormat = new DefaultFileNameFormat()
				.withPath("/" + topology_name + "/");
		RecordFormat format = new DelimitedRecordFormat()
				.withFieldDelimiter("|");

		SyncPolicy syncPolicy = new CountSyncPolicy(5);
		FileRotationPolicy rotationPolicy = new FileSizeRotationPolicy(config.getFloat("bolt.hdfs.size.rotation.policy"),
				Units.KB);

		HdfsBolt hdfsBolt = new HdfsBolt().withFsUrl(config.getString("bolt.hdfs.fs.url"))
				.withFileNameFormat(fileNameFormat).withRecordFormat(format)
				.withRotationPolicy(rotationPolicy).withSyncPolicy(syncPolicy);

		builder.setBolt("HDFSBolt", hdfsBolt, config.getInt("bolt.hdfs.parallelism.hint"))
				.shuffleGrouping("kafka-spout").setNumTasks(config.getInt("bolt.hdfs.num.tasks"));
		
		
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

/*
 * package com.opensoc.topologies;
 * 
 * import java.util.HashMap; import java.util.Map; import
 * java.util.regex.Pattern;
 * 
 * import org.apache.storm.hdfs.bolt.HdfsBolt; import
 * org.apache.storm.hdfs.bolt.format.DefaultFileNameFormat; import
 * org.apache.storm.hdfs.bolt.format.DelimitedRecordFormat; import
 * org.apache.storm.hdfs.bolt.format.FileNameFormat; import
 * org.apache.storm.hdfs.bolt.format.RecordFormat; import
 * org.apache.storm.hdfs.bolt.rotation.FileRotationPolicy; import
 * org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy; import
 * org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy.Units; import
 * org.apache.storm.hdfs.bolt.sync.CountSyncPolicy; import
 * org.apache.storm.hdfs.bolt.sync.SyncPolicy;
 * 
 * import storm.kafka.BrokerHosts; import storm.kafka.KafkaSpout; import
 * storm.kafka.SpoutConfig; import storm.kafka.StringScheme; import
 * storm.kafka.ZkHosts; import storm.kafka.bolt.KafkaBolt; import
 * backtype.storm.Config; import backtype.storm.LocalCluster; import
 * backtype.storm.StormSubmitter; import
 * backtype.storm.spout.SchemeAsMultiScheme; import
 * backtype.storm.topology.TopologyBuilder; import backtype.storm.utils.Utils;
 * 
 * import com.opensoc.enrichment.adapters.cif.CIFHbaseAdapter; import
 * com.opensoc.enrichment.adapters.lancope.LancopeHbaseAdapter; import
 * com.opensoc.enrichment.common.GenericEnrichmentBolt; import
 * com.opensoc.indexing.TelemetryIndexingBolt; import
 * com.opensoc.indexing.adapters.ESBaseBulkAdapter; import
 * com.opensoc.parsing.AbstractParserBolt; import
 * com.opensoc.parsing.TelemetryParserBolt; import
 * com.opensoc.parsing.parsers.BasicBroParser; import
 * com.opensoc.test.spouts.GenericInternalTestSpout;
 * 
 * 
 * 
 * public class BroEnrichmentTestTopology {
 * 
 * // IP Addresses' json key starts with 'id' private static final String
 * IPADDR_PATTERN =
 * "(id\\..*?:\\\")(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})";
 * 
 * //Domain is groupID 1 //Domain name should end with a quote private static
 * final String DOMAIN_NAME_PATTERN =
 * "(([A-Za-z0-9-]+)(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,}))\"";
 * 
 * //Email is group 1 private static final String EMAIL_PATTERN = "\"" +
 * "(([_A-Za-z0-9-\\+]+)(\\.[_A-Za-z0-9-]+)*@(([A-Za-z0-9-]+)(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})))"
 * + "\"";
 * 
 * public static void main(String[] args) throws Exception { TopologyBuilder
 * builder = new TopologyBuilder();
 * 
 * String topology_name = "bro"; int parallelism_hint = 1; int num_tasks = 1; //
 * int localMode = 0; // String hdfs_path = "hdfs://192.168.161.128:8020";
 * 
 * int localMode = 0; String hdfs_path = "hdfs://172.30.9.110:8020";
 * 
 * long MAX_CACHE_SIZE = 10000; long MAX_TIME_RETAIN = 10;
 * 
 * Config conf = new Config(); conf.setDebug(true);
 * 
 * // ------------KAFKA spout configuration /* BrokerHosts zk = new
 * ZkHosts("192.168.161.128:2181");
 * 
 * SpoutConfig kafkaConfig = new SpoutConfig(zk, "metadata", "/", "bro");
 * 
 * kafkaConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
 * kafkaConfig.forceFromStart = Boolean.valueOf("True");
 * kafkaConfig.startOffsetTime = -1;
 * 
 * // builder.setSpout("kafka-spout", new KafkaSpout(kafkaConfig), //
 * parallelism_hint).setNumTasks(1);
 * 
 * // EnrichmentSpout GenericInternalTestSpout testSpout = new
 * GenericInternalTestSpout()
 * .withFilename("SampleInput/BroExampleOutput").withRepeating(false);
 * 
 * builder.setSpout("EnrichmentSpout", testSpout, parallelism_hint)
 * .setNumTasks(1);
 * 
 * // ------------ParserBolt configuration
 * 
 * AbstractParserBolt parser_bolt = new TelemetryParserBolt()
 * .withMessageParser(new BasicBroParser()).withOutputFieldName( topology_name);
 * 
 * builder.setBolt("ParserBolt", parser_bolt, parallelism_hint)
 * .shuffleGrouping("EnrichmentSpout").setNumTasks(num_tasks);
 * 
 * // ------------CIF bolt configuration
 * 
 * Map<String, Pattern> cif_patterns = new HashMap<String, Pattern>();
 * Map<String, Integer> cif_pattern_ids = new HashMap<String, Integer>();
 * 
 * cif_patterns.put("IP_Address", Pattern.compile(IPADDR_PATTERN));
 * cif_pattern_ids.put("IP_Address", 2);
 * 
 * cif_patterns.put("Email_Address", Pattern.compile(EMAIL_PATTERN));
 * cif_pattern_ids.put("Email_Address", 1);
 * 
 * cif_patterns.put("Domain_Name", Pattern.compile(DOMAIN_NAME_PATTERN));
 * cif_pattern_ids.put("Domain_Name", 1);
 * 
 * GenericEnrichmentBolt cif_enrichment = new GenericEnrichmentBolt()
 * .withAdapter(new CIFHbaseAdapter()) .withOutputFieldName(topology_name)
 * .withOutputFieldName(topology_name) .withEnrichmentTag("CIF_Enrichment")
 * .withMaxTimeRetain(MAX_TIME_RETAIN)
 * .withMaxCacheSize(MAX_CACHE_SIZE).withPatterns(cif_patterns)
 * .withPatternIDs(cif_pattern_ids);
 * 
 * builder.setBolt("CIFEnrichmentBolt", cif_enrichment, parallelism_hint)
 * .shuffleGrouping("ParserBolt").setNumTasks(num_tasks);
 * 
 * // ------------Lancope bolt configuration
 * 
 * Map<String, Pattern> lancope_patterns = new HashMap<String, Pattern>();
 * lancope_patterns.put("somepattern", Pattern.compile("somevalue"));
 * lancope_patterns.put("somepattern", Pattern.compile("somevalue"));
 * 
 * GenericEnrichmentBolt lancope_enrichment = new GenericEnrichmentBolt()
 * .withAdapter(new LancopeHbaseAdapter()) .withOutputFieldName(topology_name)
 * .withEnrichmentTag("sometag") .withMaxTimeRetain(MAX_TIME_RETAIN)
 * .withMaxCacheSize(MAX_CACHE_SIZE) .withPatterns(lancope_patterns);
 * 
 * builder.setBolt("LancopeEnrichmentBolt", lancope_enrichment,
 * parallelism_hint).shuffleGrouping("CIFEnrichmentBolt")
 * .setNumTasks(num_tasks); // ------------Kafka Bolt Configuration
 * 
 * /* Map<String, String> kafka_broker_properties = new HashMap<String,
 * String>(); // add some properties?
 * 
 * conf.put("KAFKA_BROKER_PROPERTIES", kafka_broker_properties);
 * conf.put("TOPIC", topology_name + "_cnt");
 * 
 * builder.setBolt("KafkaBolt", new KafkaBolt<String, String>(),
 * parallelism_hint).shuffleGrouping("LancopeEnrichmentBolt")
 * .setNumTasks(num_tasks);
 * 
 * // ------------ES BOLT configuration
 * 
 * String ElasticSearchIP = "192.168.161.128"; int elasticSearchPort = 9300;
 * String ElasticSearchClusterName = "devo_es"; String ElasticSearchIndexName =
 * "bro_index"; String ElasticSearchDocumentName = "bro_doc"; int bulk = 200;
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
 * .shuffleGrouping("LancopeEnrichmentBolt") .setNumTasks(num_tasks);
 * 
 * // ------------HDFS BOLT configuration
 * 
 * FileNameFormat fileNameFormat = new DefaultFileNameFormat() .withPath("/" +
 * topology_name + "/"); RecordFormat format = new DelimitedRecordFormat()
 * .withFieldDelimiter("|");
 * 
 * SyncPolicy syncPolicy = new CountSyncPolicy(5); FileRotationPolicy
 * rotationPolicy = new FileSizeRotationPolicy(5.0f, Units.KB);
 * 
 * HdfsBolt hdfsBolt = new HdfsBolt().withFsUrl(hdfs_path)
 * .withFileNameFormat(fileNameFormat).withRecordFormat(format)
 * .withRotationPolicy(rotationPolicy).withSyncPolicy(syncPolicy);
 * 
 * builder.setBolt("HDFSBolt", hdfsBolt, parallelism_hint)
 * .shuffleGrouping("EnrichmentSpout").setNumTasks(num_tasks);
 * 
 * if (localMode == 1) { conf.setNumWorkers(1);
 * 
 * StormSubmitter.submitTopology(args[0], conf, builder.createTopology()); }
 * else {
 * 
 * LocalCluster cluster = new LocalCluster(); cluster.submitTopology("test",
 * conf, builder.createTopology()); // Utils.sleep(10000); //
 * cluster.killTopology("test"); cluster.shutdown(); }
 * 
 * } }
 */
