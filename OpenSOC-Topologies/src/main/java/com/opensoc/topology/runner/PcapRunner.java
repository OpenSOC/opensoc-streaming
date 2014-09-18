package com.opensoc.topology.runner;

import com.opensoc.parsing.PcapParserBolt;
import com.opensoc.test.spouts.GenericInternalTestSpout;

public class PcapRunner extends TopologyRunner{
	
	 static String test_file_path = "SampleInput/PCAPExampleOutput";


	@Override	
	public  boolean initializeTestingSpout(String name) {
		try {

			System.out.println("[OpenSOC] Initializing Test Spout");
			
			parserName = name;

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

	@Override
	boolean initializeParsingBolt(String topology_name, String name) {
		try {
			parserName = name;
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
}
