package com.opensoc.alerts.adapters;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.opensoc.alerts.interfaces.AlertsAdapter;

public class AllAlertAdapter implements AlertsAdapter, Serializable {

	Cache<String, String> cache;

	public AllAlertAdapter(Map<String, String> config) {
		try {

			int _MAX_CACHE_SIZE = Integer.parseInt(config
					.get("_MAX_CACHE_SIZE"));

			if (!config.containsKey("_MAX_TIME_RETAIN"))
				throw new Exception("_MAX_TIME_RETAIN name is missing");

			int _MAX_TIME_RETAIN = Integer.parseInt(config
					.get("_MAX_TIME_RETAIN"));

			cache = CacheBuilder.newBuilder().maximumSize(_MAX_CACHE_SIZE)
					.expireAfterWrite(_MAX_TIME_RETAIN, TimeUnit.MINUTES)
					.build();
		} catch (Exception e) {
			System.out.println("Could not initialize alerts adapter");
			e.printStackTrace();
			System.exit(0);
		}
	}

	@Override
	public boolean initialize() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean refresh() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<String, JSONObject> alert(JSONObject raw_message) {

		Map<String, JSONObject> alerts = new HashMap<String, JSONObject>();
		JSONObject content = (JSONObject) raw_message.get("message");

		JSONObject enrichment = null;

		if (raw_message.containsKey("enrichment"))
			enrichment = (JSONObject) raw_message.get("enrichment");

		JSONObject alert = new JSONObject();

		alert.put("title", "Alert from Topology");
		alert.put("priority", "4");
		alert.put("type", "error");
		alert.put("designated_host", "Uknown");
		alert.put("source", "NA");
		alert.put("dest", "NA");
		alert.put("body", "Alert from Topology");

		String source = "unknown";
		String dest = "unknown";

		if (content.containsKey("ip_src_addr"))
			source = content.get("ip_src_addr").toString();

		if (content.containsKey("ip_dst_addr"))
			dest = content.get("ip_dst_addr").toString();

		String alert_id = generateAlertId(source, dest, 0);

		alert.put("reference_id", alert_id);
		alerts.put(alert_id, alert);

		alert.put("enrichment", enrichment);

		return alerts;

	}

	@Override
	public boolean containsAlertId(String alert) {
		// TODO Auto-generated method stub
		return false;
	}

	protected String generateAlertId(String source_ip, String dst_ip,
			int alert_type) {

		String key = makeKey(source_ip, dst_ip, alert_type);

		if (cache.getIfPresent(key) != null)
			return cache.getIfPresent(key);

		String new_UUID = System.currentTimeMillis() + "-" + UUID.randomUUID();

		cache.put(key, new_UUID);
		key = makeKey(dst_ip, source_ip, alert_type);
		cache.put(key, new_UUID);

		return new_UUID;

	}

	private String makeKey(String ip1, String ip2, int alert_type) {
		return (ip1 + "-" + ip2 + "-" + alert_type);
	}
}
