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

import com.opensoc.parser.interfaces.MessageParser;

public class BasicSourcefireParser implements MessageParser, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6894149801563690057L;

	private Logger _LOG;

	@SuppressWarnings({ "unchecked", "unused" })
	public String parse(String toParse) {

		JSONObject jo = new JSONObject();
		_LOG.debug("Received message: " + toParse);

		try {
			String tmp = toParse.substring(toParse.lastIndexOf("{"));
			jo.put("key", tmp);

			String protocol = tmp.substring(tmp.indexOf("{") + 1,
					tmp.indexOf("}")).toLowerCase();
			String source = tmp.substring(tmp.indexOf("}") + 1,
					tmp.indexOf("->")).trim();
			String dest = tmp.substring(tmp.indexOf("->") + 2, tmp.length())
					.trim();

			jo.put("protocol", protocol);

			String source_ip = "";
			String dest_ip = "";

			if (source.contains(":")) {
				String parts[] = source.split(":");
				jo.put("ip_src_addr", parts[0]);
				jo.put("ip_src_port", parts[1]);
				source_ip = parts[0];
			} else {
				jo.put("ip_src_addr", source);
				source_ip = source;

			}

			if (dest.contains(":")) {
				String parts[] = dest.split(":");
				jo.put("ip_dst_addr", parts[0]);
				jo.put("ip_dst_port", parts[1]);
				dest_ip = parts[0];
			} else {
				jo.put("ip_dst_addr", dest);
				dest_ip = dest;
			}

			jo.put("timestamp", System.currentTimeMillis());
			jo.put("message", toParse.substring(0, toParse.indexOf("{")));

			String parsed = "{\"sourcefire\":" + jo.toString() + "}";
			_LOG.debug("Parsed message: " + parsed);

			return parsed;
		} catch (Exception e) {
			e.printStackTrace();
			_LOG.error("Failed to parse: " + toParse);
			return "{}";
		}
	}

	public boolean initialize(Logger LOG) {
		_LOG = LOG;
		return true;
	}

}
