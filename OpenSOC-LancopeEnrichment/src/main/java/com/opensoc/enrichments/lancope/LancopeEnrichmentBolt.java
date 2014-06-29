package com.opensoc.enrichments.lancope;

import java.io.IOException;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;

import com.opensoc.enrichments.lancope.adapters.AbstractLancopeAdapter;

@SuppressWarnings("serial")
public class LancopeEnrichmentBolt extends AbstractLancopeEnrichmentBolt{

	public LancopeEnrichmentBolt withOutputFieldName(String OutputFieldName) {
		_OutputFieldName = OutputFieldName;
		return this;
	}
	
	public LancopeEnrichmentBolt withAdapter(AbstractLancopeAdapter adapter) {
		_adapter = adapter;
		return this;
	}
	
	public void execute(Tuple arg0) {
		// TODO Auto-generated method stub
		
	}

	@SuppressWarnings("rawtypes")
	@Override
	void doPrepare(Map conf, TopologyContext topologyContext,
			OutputCollector collector) throws IOException {
		// TODO Auto-generated method stub
		
	}
	


}
