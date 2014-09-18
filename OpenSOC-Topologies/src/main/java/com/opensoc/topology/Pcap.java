package com.opensoc.topology;

import org.apache.commons.configuration.ConfigurationException;

import backtype.storm.generated.InvalidTopologyException;

import com.opensoc.topology.runner.PcapRunner;
import com.opensoc.topology.runner.TopologyRunner;

public class Pcap{
	
	public static void main(String[] args) throws ConfigurationException, Exception, InvalidTopologyException {
		
		TopologyRunner runner = new PcapRunner();
		runner.initTopology(args, "pcap");
	}
	
}
