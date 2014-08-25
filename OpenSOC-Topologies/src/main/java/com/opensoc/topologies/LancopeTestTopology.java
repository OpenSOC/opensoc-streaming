package com.opensoc.topologies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
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

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.TopologyBuilder;

import com.opensoc.enrichment.adapters.cif.CIFHbaseAdapter;
import com.opensoc.enrichment.adapters.geo.GeoMysqlAdapter;
import com.opensoc.enrichment.common.GenericEnrichmentBolt;
import com.opensoc.enrichment.host.HostAdapter;
import com.opensoc.indexing.TelemetryIndexingBolt;
import com.opensoc.indexing.adapters.ESBaseBulkAdapter;
import com.opensoc.parsing.AbstractParserBolt;
import com.opensoc.parsing.TelemetryParserBolt;
import com.opensoc.parsing.parsers.BasicLancopeParser;
import com.opensoc.tagger.interfaces.TaggerAdapter;
import com.opensoc.tagging.TelemetryTaggerBolt;
import com.opensoc.tagging.adapters.StaticAllTagger;
import com.opensoc.test.spouts.GenericInternalTestSpout;

public class LancopeTestTopology {

	public static void main(String args[]) throws Exception {
		String config_path = "";

		try {
			config_path = args[0];
		} catch (Exception e) {
			config_path = "TopologyConfigs/lancope.conf";
		}

		Configuration config = new PropertiesConfiguration(config_path);

		String topology_name = config.getString("topology.name");

		TopologyBuilder builder = new TopologyBuilder();

		Config conf = new Config();
		conf.setDebug(config.getBoolean("debug.mode"));

		// Testing Spout

		GenericInternalTestSpout testSpout = new GenericInternalTestSpout()
				.withFilename("SampleInput/LancopeExampleOutput")
				.withRepeating(false);

		builder.setSpout("EnrichmentSpout", testSpout,
				config.getInt("spout.test.parallelism.hint")).setNumTasks(
				config.getInt("spout.test.num.tasks"));

		// ------------ParserBolt configuration

		AbstractParserBolt parser_bolt = new TelemetryParserBolt()
				.withMetricConfig(config)
				.withMessageParser(new BasicLancopeParser())
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
				static_alerts_message.put("cluster",
						config.getString("bolt.alerts.cluster"));

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
								config.getString("bolt.enrichment.geo.enrichment_tag"))
						.withOutputFieldName(topology_name)
						.withAdapter(geo_adapter)
						.withMaxTimeRetain(
								config.getInt("bolt.enrichment.geo.MAX_TIME_RETAIN"))
						.withMaxCacheSize(
								config.getInt("bolt.enrichment.geo.MAX_CACHE_SIZE"))
						.withKeys(geo_keys).withMetricConfiguration(config);

				builder.setBolt("GeoEnrichBolt", geo_enrichment,
						config.getInt("bolt.enrichment.geo.parallelism.hint"))
						.shuffleGrouping("AlertsBolt")
						.setNumTasks(config.getInt("bolt.enrichment.geo.num.tasks"));
				
				// ------------Hosts Enrichment Bolt Configuration
				
				Configuration hosts = new PropertiesConfiguration("TopologyConfigs/known_hosts/known_hosts.conf");
				
				Iterator<String> keys = hosts.getKeys();
				Map<String, JSONObject> known_hosts = new HashMap<String, JSONObject>();
				JSONParser parser = new JSONParser();
				 
				    while(keys.hasNext())
				    {
				    	String key = keys.next().trim();
				    	JSONArray value = (JSONArray) parser.parse(hosts.getProperty(key).toString());
				    	known_hosts.put(key, (JSONObject) value.get(0));
				    }
				
				
				System.out.println("---------------READ IN HOSTS" + known_hosts);
				
				HostAdapter host_adapter = new HostAdapter(known_hosts);
				
				GenericEnrichmentBolt host_enrichment = new GenericEnrichmentBolt().withEnrichmentTag(config.getString("bolt.enrichment.host.enrichment_tag")).
						withAdapter(host_adapter).withMaxTimeRetain(
								config.getInt("bolt.enrichment.host.MAX_TIME_RETAIN"))
						.withMaxCacheSize(
								config.getInt("bolt.enrichment.host.MAX_CACHE_SIZE")).withOutputFieldName(topology_name)
								.withKeys(geo_keys).withMetricConfiguration(config);

				builder.setBolt("HostEnrichBolt", host_enrichment,
						config.getInt("bolt.enrichment.host.parallelism.hint"))
						.shuffleGrouping("GeoEnrichBolt")
						.setNumTasks(config.getInt("bolt.enrichment.host.num.tasks"));

		// --------------CIF Enrichment

	/*	List<String> cif_keys = new ArrayList<String>();
		cif_keys.add(config.getString("bolt.enrichment.cif.host"));

		GenericEnrichmentBolt cif_enrichment = new GenericEnrichmentBolt()
				.withAdapter(
						new CIFHbaseAdapter(
								config.getString("kafka.zk.list"),
								config.getString("kafka.zk.port"),
								config.getString("bolt.enrichment.cif.tablename")))
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
				.setNumTasks(config.getInt("bolt.enrichment.cif.num.tasks"));*/

		// * ------------HDFS BOLT For Enriched Data configuration

		FileNameFormat fileNameFormat_enriched = new DefaultFileNameFormat()
				.withPath("/" + topology_name + "_enriched/");
		RecordFormat format_enriched = new DelimitedRecordFormat()
				.withFieldDelimiter("|");

		SyncPolicy syncPolicy_enriched = new CountSyncPolicy(5);
		FileRotationPolicy rotationPolicy_enriched = new FileSizeRotationPolicy(
				5.0f, Units.KB);

		HdfsBolt hdfsBolt_enriched = new HdfsBolt()
				.withFsUrl(config.getString("bolt.hdfs.fs.url"))
				.withFileNameFormat(fileNameFormat_enriched)
				.withRecordFormat(format_enriched)
				.withRotationPolicy(rotationPolicy_enriched)
				.withSyncPolicy(syncPolicy_enriched);

		builder.setBolt("HDFSBolt_enriched", hdfsBolt_enriched,
				config.getInt("bolt.hdfs.parallelism.hint"))
				.shuffleGrouping("HostEnrichBolt")
				.setNumTasks(config.getInt("bolt.hdfs.num.tasks"));

		// ------------Indexing BOLT configuration

		TelemetryIndexingBolt indexing_bolt = new TelemetryIndexingBolt()
				.withIndexIP(config.getString("bolt.indexing.indexIP"))
				.withIndexPort(config.getInt("bolt.indexing.port"))
				.withClusterName(config.getString("bolt.indexing.clustername"))
				.withIndexName(config.getString("bolt.indexing.indexname"))
				.withDocumentName(
						config.getString("bolt.indexing.documentname"))
				.withBulk(config.getInt("bolt.indexing.bulk"))
				
				.withIndexAdapter(new ESBaseBulkAdapter());

		builder.setBolt("IndexingBolt", indexing_bolt,
				config.getInt("bolt.indexing.parallelism.hint"))
				.shuffleGrouping("HostEnrichBolt")
				.setNumTasks(config.getInt("bolt.indexing.num.tasks"));
		
		
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
