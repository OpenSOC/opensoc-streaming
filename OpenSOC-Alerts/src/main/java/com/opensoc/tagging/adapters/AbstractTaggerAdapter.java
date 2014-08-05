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

}
