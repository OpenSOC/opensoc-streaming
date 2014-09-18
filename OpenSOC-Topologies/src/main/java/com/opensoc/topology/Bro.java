package com.opensoc.topology;

import org.apache.commons.configuration.ConfigurationException;

import backtype.storm.generated.InvalidTopologyException;

import com.opensoc.topology.runner.BroRunner;
import com.opensoc.topology.runner.TopologyRunner;

public class Bro{
	
	public static void main(String[] args) throws ConfigurationException, Exception, InvalidTopologyException {
		
		TopologyRunner runner = new BroRunner();
		runner.initTopology(args, "bro");
	}
	
}
