package com.opensoc.filters;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.json.simple.JSONObject;

import com.opensoc.parser.interfaces.MessageFilter;

public class BroMessageFilter implements MessageFilter,Serializable {

	private final String _key = "protocol";
	private final Set<String> _known_protocols;

	public BroMessageFilter(Configuration conf) {
		_known_protocols = new HashSet<String>();
		List known_protocols = conf.getList("source.known.protocols");
		_known_protocols.addAll(known_protocols);
	}

	public boolean emitTuple(JSONObject message) {
		return _known_protocols.contains(message.get(_key));
	}
}
