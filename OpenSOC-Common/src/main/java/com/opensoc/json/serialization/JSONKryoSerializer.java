package com.opensoc.json.serialization;

import java.util.HashMap;
import java.util.Map;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import org.json.simple.JSONObject;

/**
 * @author kiran Custom Serializer to help Storm encode and decode JSONObjects
 */

public class JSONKryoSerializer extends
		com.esotericsoftware.kryo.Serializer<JSONObject> {

	private JSONKafkaSerializer jsonSerde = new JSONKafkaSerializer();

	@Override
	public void write(Kryo kryo, Output output, JSONObject json) {

		byte[] bytes = jsonSerde.toBytes(json);
		output.writeInt(bytes.length);
		output.write(bytes);
	}

	@Override
	public JSONObject read(Kryo kryo, Input input, Class<JSONObject> type) {

		// Get number of Entries
		int size = input.readInt();
		byte[] bytes = input.readBytes(size);

		JSONObject json = jsonSerde.fromBytes(bytes);

		return json;

	}
}
