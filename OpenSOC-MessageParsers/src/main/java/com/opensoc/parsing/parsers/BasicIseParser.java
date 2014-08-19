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
		// _parser.ReInit(new StringReader(raw_message));
		ISEParser _parser = new ISEParser("header=" + raw_message.trim());

		try {
			JSONObject output = _parser.parseObject();

			String ip_src_addr = (String) output.get("Device IP Address");
			String ip_src_port = (String) output.get("Device Port");
			String ip_dst_addr = (String) output.get("DestinationIPAddress");
			String ip_dst_port = (String) output.get("DestinationPort");

			output.put("ip_src_addr", ip_src_addr);
			output.put("ip_src_port", ip_src_port);
			output.put("ip_dst_addr", ip_dst_addr);
			output.put("ip_dst_port", ip_dst_port);

			return output;

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
