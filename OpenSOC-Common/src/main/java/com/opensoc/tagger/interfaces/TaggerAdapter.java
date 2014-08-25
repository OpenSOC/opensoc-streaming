package com.opensoc.tagger.interfaces;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public interface TaggerAdapter {

	JSONArray tag(JSONObject raw_message);
}
