package com.opensoc.tagging.adapters;

import org.json.simple.JSONObject;

public class StaticAllTagger extends AbstractTaggerAdapter{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7759427661169094065L;
	private String _static_tag_message;
	
	public StaticAllTagger(String static_tag_message)
	{
		_static_tag_message = static_tag_message;
	}

	@Override
	public JSONObject tag(JSONObject raw_message) 
	{
		return raw_message;
	}

}
