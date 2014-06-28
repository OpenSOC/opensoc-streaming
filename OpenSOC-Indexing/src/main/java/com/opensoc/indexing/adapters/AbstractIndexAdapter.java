package com.opensoc.indexing.adapters;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensoc.index.interfaces.IndexAdapter;
import com.opensoc.indexing.AbstractIndexingBolt;

public class AbstractIndexAdapter implements IndexAdapter, Serializable{
	
	protected static final Logger _LOG = LoggerFactory
			.getLogger(AbstractIndexingBolt.class);


	public boolean bulkIndex(String raw_message) {
		// TODO Auto-generated method stub
		return false;
	}


	public boolean initializeConnection(String ip, int port,
			String cluster_name, String index_name, String document_name,
			int bulk) {
		// TODO Auto-generated method stub
		return false;
	}

}
