package com.opensoc.tagging.adapters;

import org.json.simple.JSONObject;

public class StaticAllTagger extends AbstractTaggerAdapter{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7759427661169094065L;
	private JSONObject _static_tag_message;
	
	public StaticAllTagger(JSONObject static_tag_message)
	{
		_static_tag_message = static_tag_message;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject tag(JSONObject raw_message) 
	{
		raw_message.put("alert", _static_tag_message);
		return raw_message;
	}

}
