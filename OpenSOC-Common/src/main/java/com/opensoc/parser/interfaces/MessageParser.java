package com.opensoc.parser.interfaces;

import org.json.simple.JSONObject;

public interface MessageParser {
	
	void initializeParser();
	void init();
	JSONObject parse(byte[] raw_message);

}
