package com.opensoc.parser.interfaces;

import org.json.simple.JSONObject;

public interface MessageParser {
	
	void initializeParser();
	JSONObject parse(String raw_message);

}
