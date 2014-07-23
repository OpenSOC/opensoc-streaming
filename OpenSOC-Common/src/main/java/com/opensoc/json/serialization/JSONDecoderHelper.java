package com.opensoc.json.serialization;

import java.io.DataInputStream;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONDecoderHelper {

	public static String getString(DataInputStream data) throws IOException {

		int strSize = data.readInt();

		byte[] bytes = new byte[strSize];
		data.read(bytes);
		return new String(bytes);
	}

	public static Number getNumber(DataInputStream data) throws IOException {
		// only Long for now. No Floats.
		// Treating all ints,shorts, doubles as long.
		return data.readLong();
	}

	public static Boolean getBoolean(DataInputStream data) throws IOException {

		return data.readBoolean();
	}

	public static JSONArray getArray(DataInputStream data) throws IOException {
		// TODO Auto-generated method stub
		JSONArray output = new JSONArray();
		int size = data.readInt();

		for (int i = 0; i < size; i++) {
			Object value = getObject(data);
			output.add(value);
		}

		return output;
	}

	public static JSONObject getJSON(DataInputStream data) throws IOException {
		// TODO Auto-generated method stub
		JSONObject output = new JSONObject();
		int size = data.readInt();

		for (int i = 0; i < size; i++) {
			String key = (String) getObject(data);
			Object value = getObject(data);
			output.put(key, value);
		}

		return output;
	}

	public static Object getObject(DataInputStream data) throws IOException {
		// TODO Auto-generated method stub
		int objID = data.readInt();

		if (objID == 1)
			return getString(data);

		if (objID == 2)
			return getJSON(data);

		if (objID == 3)
			return getNumber(data);

		if (objID == 4)
			return getBoolean(data);

		if (objID == 5)
			return null;

		if (objID == 6)
			return getArray(data);

		return null;
	}

}
