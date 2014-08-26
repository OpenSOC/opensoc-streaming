package com.opensoc.topologies.common;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.json.simple.JSONObject;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;

import com.opensoc.json.serialization.JSONKryoSerializer;
import com.opensoc.parsing.AbstractParserBolt;
import com.opensoc.parsing.TelemetryParserBolt;
import com.opensoc.parsing.parsers.BasicBroParser;
import com.opensoc.test.spouts.GenericInternalTestSpout;
import com.opensoc.topologyhelpers.SettingsLoader;

public abstract class AbstractOpenSOCTopology 
{
	
	
	public abstract boolean initializeSpout();
}
