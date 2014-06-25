package com.opensoc.enrichments.lancope;

import java.util.Map;

import com.opensoc.enrichment.interfaces.LancopeAdapter;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;

public class LancopeEnrichmentBolt extends BaseRichBolt{
	
	public LancopeEnrichmentBolt(LancopeAdapter adapter)
	{
		
	}

	public void execute(Tuple arg0) {
		// TODO Auto-generated method stub
		
	}

	public void prepare(Map arg0, TopologyContext arg1, OutputCollector arg2) {
		// TODO Auto-generated method stub
		
	}

	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		// TODO Auto-generated method stub
		
	}

}
