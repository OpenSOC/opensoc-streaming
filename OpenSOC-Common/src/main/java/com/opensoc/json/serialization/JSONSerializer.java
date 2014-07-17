package com.opensoc.json.serialization;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import org.json.simple.JSONObject;

import com.esotericsoftware.kryo.serializers.MapSerializer;

/**
 * @author kiran Custom Serializer to help Storm encode and decode JSONObjects
 */

public class JSONSerializer extends
		com.esotericsoftware.kryo.Serializer<JSONObject> {

	@Override
	public void write(Kryo kryo, Output output, JSONObject json) {

		HashMap<String, String> jsonMap = (HashMap) json;

		// put number of entries
		output.writeInt(jsonMap.size());

		for (Map.Entry<String, String> entry : jsonMap.entrySet()) {
			output.writeString(entry.getKey());
			output.writeString(entry.getValue());
		}
	}

	@Override
	public JSONObject read(Kryo kryo, Input input, Class<JSONObject> type) {

		JSONObject json = new JSONObject();

		// Get number of Entries
		int size = input.readInt();

		for (int i = 0; i < size; i++) {
			String key = input.readString();
			String val = input.readString();
			json.put(key, val);
		}

		return json;
		
	}
}
