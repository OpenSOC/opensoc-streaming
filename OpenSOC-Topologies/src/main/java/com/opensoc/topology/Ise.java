package com.opensoc.topology;

import org.apache.commons.configuration.ConfigurationException;

import backtype.storm.generated.InvalidTopologyException;

import com.opensoc.topology.runner.ISERunner;
import com.opensoc.topology.runner.TopologyRunner;

public class Ise{
	
	public static void main(String[] args) throws ConfigurationException, Exception, InvalidTopologyException {
		
		TopologyRunner runner = new ISERunner();
		runner.initTopology(args, "ise");
	}
	
}
