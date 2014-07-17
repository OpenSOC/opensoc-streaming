package com.opensoc.topologies;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import backtype.storm.Config;
import backtype.storm.topology.TopologyBuilder;

import com.opensoc.parsing.AbstractParserBolt;
import com.opensoc.parsing.TelemetryParserBolt;
import com.opensoc.parsing.parsers.BasicLancopeParser;
import com.opensoc.test.spouts.GenericInternalTestSpout;

public class LancopeTestTopology {

	public static void main(String args[]) throws ConfigurationException {
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
		conf.setDebug(config.getBoolean("debug.mode"));

		// Testing Spout

		GenericInternalTestSpout testSpout = new GenericInternalTestSpout()
				.withFilename("SampleInput/LancopeExampleOutput").withRepeating(
						false);

		builder.setSpout("EnrichmentSpout", testSpout,
				config.getInt("spout.test.parallelism.hint")).setNumTasks(
				config.getInt("spout.test.num.tasks"));

		// ------------ParserBolt configuration

		AbstractParserBolt parser_bolt = new TelemetryParserBolt()
				.withMessageParser(new BasicLancopeParser())
				.withOutputFieldName(topology_name);

		builder.setBolt("ParserBolt", parser_bolt,
				config.getInt("bolt.parser.parallelism.hint"))
				.shuffleGrouping("kafka-spout")
				.setNumTasks(config.getInt("bolt.parser.num.tasks"));

	}

}
