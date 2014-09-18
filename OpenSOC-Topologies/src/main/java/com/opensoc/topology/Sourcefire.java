package com.opensoc.topology;

import org.apache.commons.configuration.ConfigurationException;

import com.opensoc.topology.runner.SourcefireRunner;
import com.opensoc.topology.runner.TopologyRunner;

import backtype.storm.generated.InvalidTopologyException;

public class Sourcefire{
	
	public static void main(String[] args) throws ConfigurationException, Exception, InvalidTopologyException {
		
		TopologyRunner runner = new SourcefireRunner();
		runner.initTopology(args, "sourcefire");
	}
	
}
