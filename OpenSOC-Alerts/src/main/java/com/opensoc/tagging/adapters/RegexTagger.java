package com.opensoc.tagging.adapters;

import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class RegexTagger extends AbstractTaggerAdapter{
	
	Map <String, JSONObject> _rules;
	
	public RegexTagger(Map<String, JSONObject> rules)
	{
		_rules = rules;
	}

	public JSONArray tag(JSONObject raw_message) {
		

		JSONArray ja = new JSONArray();
		String message_as_string = raw_message.toString();
		
		for(String rule : _rules.keySet())
		{		
			if (message_as_string.matches(rule))
			{
				ja.add(_rules.get(rule));
			}
		}	
		
		return ja;
	}

}
