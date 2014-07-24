package com.opensoc.json.serialization;

import java.io.DataOutputStream;
import java.io.IOException;

import org.json.simple.JSONArray;

public class JSONEncoderHelper {

	public static void putNull(DataOutputStream data, Object value)
			throws IOException {
		// TODO Auto-generated method stub
		data.writeInt(5);

	}

	public static void putBoolean(DataOutputStream data, Boolean value)
			throws IOException {
		// TODO Auto-generated method stub
		data.writeInt(4);
		data.writeBoolean(value);

	}

	public static void putNumber(DataOutputStream data, Number value)
			throws IOException {
		// TODO Auto-generated method stub
		data.writeInt(3);
		if (value instanceof Double)
		{
			data.writeByte(0);
			data.writeDouble((Double) value);
			return;
		}
		data.writeByte(1);
		data.writeLong((Long) value);

	}

	public static void putString(DataOutputStream data, String str)
			throws IOException {
		// String ID is 1
		data.writeInt(1);
		data.writeInt(str.length());
		data.write(str.getBytes());

	}

}
