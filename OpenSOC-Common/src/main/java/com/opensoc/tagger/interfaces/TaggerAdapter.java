package com.opensoc.tagger.interfaces;

import org.json.simple.JSONObject;

public interface TaggerAdapter {

	JSONObject tag(JSONObject raw_message);
}
