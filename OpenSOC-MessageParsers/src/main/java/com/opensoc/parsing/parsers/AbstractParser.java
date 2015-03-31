/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opensoc.parsing.parsers;

import java.io.Serializable;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensoc.parser.interfaces.MessageParser;
import com.opensoc.parsing.AbstractParserBolt;

@SuppressWarnings("serial")
public abstract class AbstractParser implements MessageParser, Serializable {

	protected static final Logger _LOG = LoggerFactory
			.getLogger(AbstractParserBolt.class);

	public void initializeParser() {
		_LOG.debug("Initializing adapter...");
		

	}
	
	public void init() {
		
	}
	
	
	abstract public JSONObject parse(byte[] raw_message);

}
