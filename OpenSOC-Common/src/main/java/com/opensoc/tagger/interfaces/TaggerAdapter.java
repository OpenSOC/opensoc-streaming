package com.opensoc.tagger.interfaces;

import org.json.simple.JSONObject;

public interface TaggerAdapter {

	String tag(JSONObject raw_message);
}
