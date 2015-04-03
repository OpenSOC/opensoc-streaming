/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.opensoc.topology.runner;

import com.opensoc.parsing.PcapParserBolt;
import com.opensoc.test.spouts.GenericInternalTestSpout;

public class PcapRunner extends TopologyRunner{
	
	 static String test_file_path = "SampleInput/PCAPExampleOutput";


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

	@Override
	boolean initializeParsingBolt(String topology_name, String name) {
		try {

			String messageUpstreamComponent = messageComponents.get(messageComponents.size()-1);
			
			System.out.println("[OpenSOC] ------" +  name + " is initializing from " + messageUpstreamComponent);
			
			PcapParserBolt pcapParser = new PcapParserBolt().withTsPrecision(config.getString("bolt.parser.ts.precision"));
			
			builder.setBolt(name, pcapParser,
					config.getInt("bolt.parser.parallelism.hint"))
					.setNumTasks(config.getInt("bolt.parser.num.tasks"))
					.shuffleGrouping(messageUpstreamComponent);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		return true;
	}
}
