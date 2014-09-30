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
import org.json.simple.JSONValue;

@SuppressWarnings("serial")
public class BasicLancopeParser extends AbstractParser {
	// Sample Lancope Message
	// {"message":"<131>Jul 17 15:59:01 smc-01 StealthWatch[12365]: 2014-07-17T15:58:30Z 10.40.10.254 0.0.0.0 Minor High Concern Index The host's concern index has either exceeded the CI threshold or rapidly increased. Observed 36.55M points. Policy maximum allows up to 20M points.","@version":"1","@timestamp":"2014-07-17T15:56:05.992Z","type":"syslog","host":"10.122.196.201"}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject parse(String raw_message) {

		JSONObject payload;
		payload = (JSONObject) JSONValue.parse(raw_message);

		JSONObject output = new JSONObject();
		output.put("lancope", payload);

		return output;
	}

}
