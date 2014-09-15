package com.opensoc.filters;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.json.simple.JSONObject;

import com.opensoc.parser.interfaces.MessageFilter;

public class BroMessageFilter implements MessageFilter, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String _key = "protocol";
	private final Set<String> _include_protocols, _exclude_protocols;
	private boolean include_all = false;
	private boolean exclude_none = false;

	public BroMessageFilter(Configuration conf) {
		_include_protocols = new HashSet<String>();
		_exclude_protocols = new HashSet<String>();
		List include_protocols = conf.getList("source.include.protocols", null);
		List exclude_protocols = conf.getList("source.exclude.protocols", null);

		if (null == include_protocols)
			include_all = true;
		else
			_include_protocols.addAll(include_protocols);

		if (null == exclude_protocols)
			exclude_none = true;
		else
			_exclude_protocols.addAll(exclude_protocols);

		if (_include_protocols.contains("*"))
			include_all = true;
	}

	public boolean emitTuple(JSONObject message) {
		if (include_all)
			return true;

		if (_include_protocols.contains(message.get(_key)))
			return true;

		if (exclude_none)
			return true;

		if (_exclude_protocols.contains(message.get(_key)))
			return false;

		return false;

	}
}
