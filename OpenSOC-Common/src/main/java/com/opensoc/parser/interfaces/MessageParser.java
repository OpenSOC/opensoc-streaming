package com.opensoc.parser.interfaces;

import org.slf4j.Logger;

public interface MessageParser {
	
	boolean initialize(Logger LOG);
	String parse(String raw_message);

}
