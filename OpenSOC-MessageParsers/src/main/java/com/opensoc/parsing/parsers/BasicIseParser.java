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

import com.esotericsoftware.minlog.Log;
import com.opensoc.ise.parser.ISEParser;

@SuppressWarnings("serial")
public class BasicIseParser extends AbstractParser {

	protected static final Logger _LOG = LoggerFactory
			.getLogger(BasicIseParser.class);
	static final transient ISEParser _parser = new ISEParser("header=");

	@SuppressWarnings("unchecked")
	public JSONObject parse(byte[] msg) {
	
		String raw_message = "";

		try {

			raw_message = new String(msg, "UTF-8");
			_LOG.debug("Received message: " + raw_message);
			
			/*
			 * Reinitialize Parser. It has the effect of calling the constructor again. 
			 */
			_parser.ReInit(new StringReader("header=" + raw_message.trim()));

			JSONObject payload = _parser.parseObject();

			String ip_src_addr = (String) payload.get("Device IP Address");
			String ip_src_port = (String) payload.get("Device Port");
			String ip_dst_addr = (String) payload.get("DestinationIPAddress");
			String ip_dst_port = (String) payload.get("DestinationPort");
			
			/*
			 * Standard Fields for OpenSoc.
			 */

			if(ip_src_addr != null)
				payload.put("ip_src_addr", ip_src_addr);
			if(ip_src_port != null)
				payload.put("ip_src_port", ip_src_port);
			if(ip_dst_addr != null)
				payload.put("ip_dst_addr", ip_dst_addr);
			if(ip_dst_port != null)
				payload.put("ip_dst_port", ip_dst_port);

			JSONObject message = new JSONObject();
			//message.put("message", payload);

			return payload;

		} catch (Exception e) {
			Log.error(e.toString());
			e.printStackTrace();
		}
		return null;
	}

	
}
