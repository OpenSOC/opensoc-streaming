package com.opensoc.alerts.adapters;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensoc.alerts.interfaces.AlertsAdapter;

public class HbaseWhiteAndBlacklistAdapter implements AlertsAdapter,
		Serializable {

	HTableInterface blacklist_table;
	HTableInterface whitelist_table;
	InetAddressValidator ipvalidator = new InetAddressValidator();
	String _whitelist_table_name;
	String _blacklist_table_name;
	String _quorum;
	String _port;
	String _topologyname;
	Configuration conf = null;

	AlertsCache<String, String> alerts_cache;
	String _topology_name;

	protected static final Logger LOG = LoggerFactory
			.getLogger(HbaseWhiteAndBlacklistAdapter.class);

	public HbaseWhiteAndBlacklistAdapter(String whitelist_table_name,
			String blacklist_table_name, String quorum, String port,
			int _MAX_TIME_RETAIN, int _MAX_CACHE_SIZE) {

		_whitelist_table_name = whitelist_table_name;
		_blacklist_table_name = blacklist_table_name;
		_quorum = quorum;
		_port = port;

		alerts_cache = new AlertsCache<String, String>(_MAX_TIME_RETAIN, 60,
				_MAX_CACHE_SIZE);

	}
	


	public boolean containsAlertId(String key) {

			
			System.out.println("LOOKING FOR VALUE: " + key + " in cache " + alerts_cache.toString());
			

			
			return alerts_cache.containsValue(key);
		
	}

	public boolean initialize() {

		conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", _quorum);
		conf.set("hbase.zookeeper.property.clientPort", _port);

		System.out.println("--------ALERTS CONNECTING TO HBASE WITH: " + conf);

		System.out.println("--------whitelist: " + _whitelist_table_name);
		System.out.println("--------blacklist: " + _blacklist_table_name);

		System.out.println("--------hbase.zookeeper.quorum: "
				+ conf.get("hbase.zookeeper.quorum"));
		System.out.println("--------hbase.zookeeper.property.clientPort: "
				+ conf.get("hbase.zookeeper.property.clientPort"));
		try {

			System.out.println("--------ALERTS CONNECTING TO HBASE WITH: "
					+ conf);

			HConnection connection = HConnectionManager.createConnection(conf);

			System.out.println("--------CONNECTED TO HBASE");

			HBaseAdmin hba = new HBaseAdmin(conf);

			if (!hba.tableExists(_whitelist_table_name))
				throw new Exception("Whitelist table doesn't exist");

			if (!hba.tableExists(_blacklist_table_name))
				throw new Exception("Blacklist table doesn't exist");

			whitelist_table = new HTable(conf, _whitelist_table_name);

			System.out.println("--------CONNECTED TO TABLE: "
					+ _whitelist_table_name);
			blacklist_table = new HTable(conf, _blacklist_table_name);
			System.out.println("--------CONNECTED TO TABLE: "
					+ _blacklist_table_name);

			if (connection == null || whitelist_table == null
					|| blacklist_table == null)
				throw new Exception("Unable to initialize hbase connection");

			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;

	}

	protected String generateAlertId(String source_ip, String dst_ip,
			int alert_type) {

		String key = makeKey(source_ip, dst_ip, alert_type);

		if (alerts_cache.containsKey(key))
			return alerts_cache.get(key);

		String new_UUID = System.currentTimeMillis() + "-" + UUID.randomUUID();

		alerts_cache.put(key, new_UUID);
		key = makeKey(dst_ip, source_ip, alert_type);
		alerts_cache.put(key, new_UUID);

		return new_UUID;

	}

	public boolean getByKey(String metadata, HTableInterface table) {

		LOG.debug("=======Pinging HBase For:" + metadata);

		System.out.println("--------HBASE LIST LOOKUP: " + metadata);

		System.out.println(table);

		Get get = new Get(metadata.getBytes());
		Result rs;

		try {
			rs = table.get(get);

			if (rs.size() > 0)
				return true;
			else
				return false;

		} catch (IOException e) {

			e.printStackTrace();
		}

		return false;

	}

	public boolean refresh() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	private String makeKey(String ip1, String ip2, int alert_type) {
		return (ip1 + "-" + ip2 + "-" + alert_type);
	}

	@SuppressWarnings("unchecked")
	public Map<String, JSONObject> alert(JSONObject raw_message) {

		Map<String, JSONObject> alerts = new HashMap<String, JSONObject>();

		JSONObject content = (JSONObject) raw_message.get("message");

		String source_ip = content.get("ip_src_addr").toString();
		String dst_ip = content.get("ip_dst_addr").toString();

		if (source_ip == null && dst_ip == null) {

			int alert_type = 0;

			JSONObject alert = new JSONObject();

			alert.put("title", "IP Check Error Type: : " + alert_type);
			alert.put("priority", "1");
			alert.put("type", "error");
			alert.put("source", "Uknown");
			alert.put(
					"body",
					"This communication does not contain a source or destination IP string. Communication between two IPs: "
							+ source_ip + " -> " + dst_ip);

			String alert_id = generateAlertId(source_ip, dst_ip, alert_type);

			alert.put("reference_id", alert_id);
			alerts.put(alert_id, alert);

			return alerts;

		}

		if (!ipvalidator.isValidInet4Address(source_ip)
				&& !ipvalidator.isValidInet4Address(dst_ip)) {
			int alert_type = 1;

			JSONObject alert = new JSONObject();

			alert.put("title", "IP Check Error Type: : " + alert_type);
			alert.put("priority", "1");
			alert.put("type", "error");
			alert.put("source", "Uknown");
			alert.put(
					"content",
					"This communication contains souce and destination IP strings, but these strings are not valid. Communication between two IPs: "
							+ source_ip + " -> " + dst_ip);

			String alert_id = generateAlertId(source_ip, dst_ip, alert_type);

			alert.put("reference_id", alert_id);
			alerts.put(alert_id, alert);

			return alerts;

		}

		boolean source_in_white_list = false;
		boolean dest_in_white_list = false;
		boolean source_in_black_list = true;
		boolean dest_in_black_list = true;

		String designated_host = null;

		if (ipvalidator.isValidInet4Address(source_ip)) {
			source_in_white_list = getByKey(source_ip, whitelist_table);
			source_in_black_list = getByKey(source_ip, blacklist_table);
		}

		if (ipvalidator.isValidInet4Address(dst_ip)) {
			dest_in_white_list = getByKey(dst_ip, whitelist_table);
			dest_in_black_list = getByKey(dst_ip, blacklist_table);
		}

		if (source_in_white_list)
			designated_host = source_ip;

		else if (dest_in_white_list)
			designated_host = dst_ip;

		if (designated_host == null) {
			int alert_type = 3;

			JSONObject alert = new JSONObject();

			alert.put("title", "IP Check Error Type: : " + alert_type);
			alert.put("priority", "1");
			alert.put("type", "error");
			alert.put("source", "Uknown");
			alert.put(
					"content",
					"This communication does not contain a source or a destination IP that is in the white list. Communication between two IPs: "
							+ source_ip + " -> " + dst_ip);

			String alert_id = generateAlertId(source_ip, dst_ip, alert_type);

			alert.put("reference_id", alert_id);
			alerts.put(alert_id, alert);

			return alerts;

		}

		if (source_ip.equals(designated_host)
				&& !ipvalidator.isValidInet4Address(dst_ip)) {
			int alert_type = 4;

			JSONObject alert = new JSONObject();

			alert.put("title", "IP Check Error Type: : " + alert_type);
			alert.put("priority", "1");
			alert.put("type", "error");
			alert.put("source", designated_host);
			alert.put(
					"content",
					"This communication contains an IP that is not valid. Communication between two IPs: "
							+ source_ip + " -> " + dst_ip);

			String alert_id = generateAlertId(source_ip, dst_ip, alert_type);

			alert.put("reference_id", alert_id);
			alerts.put(alert_id, alert);

		}

		if (dst_ip.equals(designated_host)
				&& !ipvalidator.isValidInet4Address(source_ip)) {
			int alert_type = 5;

			JSONObject alert = new JSONObject();

			alert.put("title", "IP Check Error Type: : " + alert_type);
			alert.put("priority", "1");
			alert.put("type", "error");
			alert.put("source", designated_host);
			alert.put(
					"content",
					"This communication contains IP that is not valid. Communication between two IPs: "
							+ source_ip + " -> " + dst_ip);

			String alert_id = generateAlertId(source_ip, dst_ip, alert_type);

			alert.put("reference_id", alert_id);
			alerts.put(alert_id, alert);

		}

		if (source_in_black_list == true) {
			int alert_type = 6;

			JSONObject alert = new JSONObject();

			alert.put("title", "IP Check Error Type: : " + alert_type);
			alert.put("priority", "1");
			alert.put("type", "error");
			alert.put("source", designated_host);
			alert.put(
					"content",
					"This communication contains IP that is black listed. Communication between two IPs: "
							+ source_ip + " -> " + dst_ip);

			String alert_id = generateAlertId(source_ip, dst_ip, alert_type);

			alert.put("reference_id", alert_id);
			alerts.put(alert_id, alert);

		}

		if (dest_in_black_list == true) {
			int alert_type = 7;

			JSONObject alert = new JSONObject();

			alert.put("title", "IP Check Error Type: : " + alert_type);
			alert.put("priority", "1");
			alert.put("type", "error");
			alert.put("source", designated_host);
			alert.put(
					"content",
					"This communication contains IP that is black listed. Communication between two IPs: "
							+ source_ip + " -> " + dst_ip);

			String alert_id = generateAlertId(source_ip, dst_ip, alert_type);

			alert.put("reference_id", alert_id);
			alerts.put(alert_id, alert);

		}

		if (alerts.isEmpty())
			return null;
		else
			return alerts;
	}

}
