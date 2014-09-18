package com.opensoc.topology.runner;

import com.opensoc.filters.GenericMessageFilter;
import com.opensoc.parsing.AbstractParserBolt;
import com.opensoc.parsing.TelemetryParserBolt;
import com.opensoc.parsing.parsers.BasicIseParser;
import com.opensoc.test.spouts.GenericInternalTestSpout;

public class ISERunner extends TopologyRunner{
	
	 static String test_file_path = "SampleInput/ISESampleOutput";

	@Override
	public boolean initializeParsingBolt(String topology_name,
			String name) {
		try {

			AbstractParserBolt parser_bolt = new TelemetryParserBolt()
					.withMessageParser(new BasicIseParser())
					.withOutputFieldName(topology_name)
					.withMessageFilter(new GenericMessageFilter())
					.withMetricConfig(config);

			builder.setBolt(name, parser_bolt,
					config.getInt("bolt.parser.parallelism.hint"))
					.shuffleGrouping(component)
					.setNumTasks(config.getInt("bolt.parser.num.tasks"));

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		return true;
	}

	@Override	
	public  boolean initializeTestingSpout(String name) {
		try {

			System.out.println("[OpenSOC] Initializing Test Spout");

			GenericInternalTestSpout testSpout = new GenericInternalTestSpout()
					.withFilename(test_file_path).withRepeating(
							config.getBoolean("spout.test.parallelism.repeat"));

			builder.setSpout(name, testSpout,
					config.getInt("spout.test.parallelism.hint")).setNumTasks(
					config.getInt("spout.test.num.tasks"));

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return true;
	}
}
