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

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

@SuppressWarnings("serial")
public class BasicSourcefireParser extends AbstractParser{

	@SuppressWarnings({ "unchecked", "unused", "rawtypes" })
	public JSONObject parse(String toParse) {

		Map jo = new HashMap();
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
			
			JSONObject output = new JSONObject();
			
			output.put("sourcefire", jo);
			
			//String parsed = "{\"sourcefire\":" + jo.toString() + "}";
			_LOG.debug("Parsed message: " + output);

			//return parsed;
			return output;
		} catch (Exception e) {
			e.printStackTrace();
			_LOG.error("Failed to parse: " + toParse);
			return new JSONObject();
			//return null;
		}
	}

}
