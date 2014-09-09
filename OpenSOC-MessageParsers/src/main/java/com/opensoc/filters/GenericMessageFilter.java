package com.opensoc.filters;
import java.io.Serializable;

import org.json.simple.JSONObject;

import com.opensoc.parser.interfaces.MessageFilter;

public class GenericMessageFilter implements MessageFilter,Serializable {

	public boolean emitTuple(JSONObject message) {
		return true;
	}

}
