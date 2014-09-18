package com.opensoc.topology;

import org.apache.commons.configuration.ConfigurationException;

import backtype.storm.generated.InvalidTopologyException;

import com.opensoc.topology.runner.LancopeRunner;
import com.opensoc.topology.runner.TopologyRunner;

public class Lancope{
	
	public static void main(String[] args) throws ConfigurationException, Exception, InvalidTopologyException {
		
		TopologyRunner runner = new LancopeRunner();
		runner.initTopology(args, "lancope");
	}
	
}
