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

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class BasicBroParser extends AbstractParser
{
	
	//private Logger _LOG ;
	protected static final Logger _LOG = LoggerFactory
			.getLogger(BasicBroParser.class);
	JSONCleaner cleaner = new JSONCleaner();

	public String parse(String raw_message) 
	{
		_LOG.debug("Received message: " + raw_message);
		
		try {
			String cleaned_message = cleaner.Clean(raw_message);
			_LOG.debug("Cleaned message: " + cleaned_message);
			return cleaned_message;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			_LOG.error("Unable to Parse Message: " + raw_message);
			e.printStackTrace();
		}
		return null;
	}

}
