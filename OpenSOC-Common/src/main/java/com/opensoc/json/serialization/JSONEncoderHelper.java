package com.opensoc.json.serialization;

import java.io.DataOutputStream;
import java.io.IOException;

public class JSONEncoderHelper {

	public static void putNull(DataOutputStream data, Object value)
			throws IOException {
		// TODO Auto-generated method stub
		data.writeByte(JSONKafkaSerializer.NULLID);

	}

	public static void putBoolean(DataOutputStream data, Boolean value)
			throws IOException {
		// TODO Auto-generated method stub
		data.writeByte(JSONKafkaSerializer.BooleanID);
		data.writeBoolean(value);

	}

	public static void putNumber(DataOutputStream data, Number value)
			throws IOException {
		// TODO Auto-generated method stub
		data.writeByte(JSONKafkaSerializer.NumberID);
		if (value instanceof Double) {
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
		data.writeByte(JSONKafkaSerializer.StringID);
		data.writeInt(str.length());
		data.write(str.getBytes());

	}

}
