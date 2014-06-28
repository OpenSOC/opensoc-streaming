package com.opensoc.parser.interfaces;

public interface MessageParser {
	
	void initializeParser();
	String parse(String raw_message);

}
