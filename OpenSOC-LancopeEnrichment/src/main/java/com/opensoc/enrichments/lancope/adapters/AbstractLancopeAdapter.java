package com.opensoc.enrichments.lancope.adapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensoc.enrichment.interfaces.LancopeAdapter;

public abstract class AbstractLancopeAdapter implements LancopeAdapter{

	protected static final Logger LOG = LoggerFactory
			.getLogger(AbstractLancopeAdapter.class);
	
	abstract public boolean initializeAdapter(String ip);


}
