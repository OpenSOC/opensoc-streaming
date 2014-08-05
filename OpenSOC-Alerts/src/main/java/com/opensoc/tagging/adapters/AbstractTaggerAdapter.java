package com.opensoc.tagging.adapters;

import java.io.Serializable;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensoc.tagger.interfaces.TaggerAdapter;

@SuppressWarnings("serial")
public abstract class AbstractTaggerAdapter implements TaggerAdapter, Serializable{
	
	protected static final Logger _LOG = LoggerFactory
			.getLogger(AbstractTaggerAdapter.class);


	public abstract boolean bulkIndex(JSONObject raw_message);
	public abstract boolean bulkIndex(String raw_message);
	

	abstract public boolean initializeConnection(String ip, int port,
			String cluster_name, String index_name, String document_name,
			int bulk);

}
