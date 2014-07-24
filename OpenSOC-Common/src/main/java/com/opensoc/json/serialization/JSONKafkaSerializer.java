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

package com.opensoc.json.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import kafka.serializer.Decoder;
import kafka.serializer.Encoder;
import kafka.utils.VerifiableProperties;
import static com.opensoc.json.serialization.JSONEncoderHelper.*;
import static com.opensoc.json.serialization.JSONDecoderHelper.*;

/**
 * JSON Serailization class for kafka. Implements kafka Encoder and Decoder
 * String, JSONObject, Number, Boolean,JSONObject.NULL JSONArray
 * 
 * @author kiran
 * 
 */

public class JSONKafkaSerializer implements Encoder<JSONObject>,
		Decoder<JSONObject> {
	
	public JSONKafkaSerializer() {

	}

	public JSONKafkaSerializer(VerifiableProperties props) {

	}

	public static void main(String args[]) {
		String jsonString = "{\"dns\":{\"ts\":[14.0,12,\"kiran\"],\"uid\":\"abullis@mail.csuchico.edu\",\"id.orig_h\":\"10.122.196.204\", \"endval\":null}}";

		JSONParser p = new JSONParser();
		JSONObject json = null;
		try {
			json = (JSONObject) p.parse(jsonString);
			System.out.println(json);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JSONKafkaSerializer ser = new JSONKafkaSerializer();

		byte[] bytes = ser.toBytes(json);

		String jsonString2 = ser.fromBytes(bytes).toJSONString();
		
		System.out.println((jsonString2));
		System.out.println(jsonString2.equalsIgnoreCase(json.toJSONString()));

	}

	

	public JSONObject fromBytes(byte[] input) {
		// TODO Auto-generated method stub

		ByteArrayInputStream inputBuffer = new ByteArrayInputStream(input);
		DataInputStream data = new DataInputStream(inputBuffer);

		JSONObject output = new JSONObject();

		try {
			int mapSize = data.readInt();

			for (int i = 0; i < mapSize; i++) {
				String key = (String) getObject(data);
				// System.out.println("Key Found"+ key);
				Object val = getObject(data);
				output.put(key, val);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return output;
	}

	public byte[] toBytes(JSONObject input) {
		// TODO Auto-generated method stub

		ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(outputBuffer);

		Iterator it = input.entrySet().iterator();
		try {

			// write num of entries
			data.writeInt(input.size());

			// Write every single entry in hashmap
			while (it.hasNext()) {
				Map.Entry<String, Object> entry = (Entry<String, Object>) it
						.next();
				putObject(data, entry.getKey());
				putObject(data, entry.getValue());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return outputBuffer.toByteArray();
	}
	
	

	private void putObject(DataOutputStream data, Object value)
			throws IOException {
		// TODO Auto-generated method stub

		if (value instanceof JSONObject) {
			putJSON(data, (JSONObject) value);
			return;

		}

		if (value instanceof String) {
			putString(data, (String) value);
			return;
		}

		if (value instanceof Number) {
			putNumber(data, (Number) value);
			return;
		}

		if (value instanceof Boolean) {
			putBoolean(data, (Boolean) value);
			return;
		}

		if (value == null) {
			putNull(data, value);
			return;
		}

		if (value instanceof JSONArray) {
			putArray(data, (JSONArray) value);
			return;
		}

	}

	private void putJSON(DataOutputStream data, JSONObject value)
			throws IOException {
		// TODO Auto-generated method stub
		// JSON ID is 2
		data.writeInt(2);
		data.write(toBytes(value));

	}

	public void putArray(DataOutputStream data, JSONArray array)
			throws IOException {
		// TODO Auto-generated method stub
		data.writeInt(6);

		data.writeInt(array.size());

		for (Object o : array)
			putObject(data, o);

	}

}