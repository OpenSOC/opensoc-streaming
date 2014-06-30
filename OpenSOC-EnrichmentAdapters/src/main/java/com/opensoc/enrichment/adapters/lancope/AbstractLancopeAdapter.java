package com.opensoc.enrichment.adapters.lancope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensoc.enrichment.common.EnrichmentAdapter;

public abstract class AbstractLancopeAdapter implements EnrichmentAdapter{

	protected static final Logger LOG = LoggerFactory
			.getLogger(AbstractLancopeAdapter.class);
	
	abstract public boolean initializeAdapter();


}
