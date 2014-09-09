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
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class BasicBroParser extends AbstractParser {

	// private Logger _LOG ;
	protected static final Logger _LOG = LoggerFactory
			.getLogger(BasicBroParser.class);
	JSONCleaner cleaner = new JSONCleaner();

	public JSONObject parse(byte[] msg) {

		String raw_message = "";

		try {

			raw_message = new String(msg, "UTF-8");

			_LOG.debug("Received message: " + raw_message);

			JSONObject cleaned_message = cleaner.Clean(raw_message);

			String key = cleaned_message.keySet().iterator().next().toString();

			JSONObject payload = (JSONObject) cleaned_message.get(key);

			if (payload.containsKey("id.orig_h")) {
				String source_ip = payload.remove("id.orig_h").toString();
				payload.put("ip_src_addr", source_ip);
			}
			if (payload.containsKey("id.resp_h")) {
				String source_ip = payload.remove("id.resp_h").toString();
				payload.put("ip_dst_addr", source_ip);
			}
			if (payload.containsKey("id.orig_p")) {
				String source_port = payload.remove("id.orig_p")
						.toString();
				payload.put("ip_src_port", source_port);
			}
			if (payload.containsKey("id.resp_p")) {
				String dest_port = payload.remove("id.resp_p").toString();
				payload.put("ip_dst_port", dest_port);
			}
			if (payload.containsKey("host")) {

				String host = payload.get("host").toString().trim();
				String[] parts = host.split("\\.");
				int length = parts.length;
				payload.put("tld", parts[length - 2] + "."
						+ parts[length - 1]);
			}
			if (payload.containsKey("query")) {
				String host = payload.get("query").toString();
				String[] parts = host.split("\\.");
				int length = parts.length;
				payload.put("tld", parts[length - 2] + "."
						+ parts[length - 1]);
			}

			_LOG.debug("Inner message: " + payload);

			payload.put("protocol", key);

			return payload;
		} catch (Exception e) {

			_LOG.error("Unable to Parse Message: " + raw_message);
			e.printStackTrace();
		}
		return null;
	}

}
