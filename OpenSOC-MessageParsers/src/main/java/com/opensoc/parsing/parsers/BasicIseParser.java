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

import java.io.StringReader;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensoc.ise.parser.ISEParser;
import com.opensoc.ise.parser.ParseException;

@SuppressWarnings("serial")
public class BasicIseParser extends AbstractParser {

	protected static final Logger _LOG = LoggerFactory
			.getLogger(BasicIseParser.class);

	

	public JSONObject parse(String raw_message) {
		_LOG.debug("Received message: " + raw_message);
		//_parser.ReInit(new StringReader(raw_message));
		ISEParser _parser = new ISEParser("header=" + raw_message.trim());
		
		try {
			return _parser.parseObject();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
