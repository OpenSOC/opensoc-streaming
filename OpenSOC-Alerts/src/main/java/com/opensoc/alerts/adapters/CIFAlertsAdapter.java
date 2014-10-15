package com.opensoc.alerts.adapters;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.opensoc.alerts.interfaces.AlertsAdapter;

public class CIFAlertsAdapter implements AlertsAdapter, Serializable {
	
	String enrichment_tag;
	Cache<String, String> cache;
	
	public CIFAlertsAdapter(Map<String, String> config)
	{
		try
		{
		enrichment_tag = config.get("enrichment_tag");
		
		int _MAX_CACHE_SIZE = Integer.parseInt(config
				.get("_MAX_CACHE_SIZE"));
		
		if(!config.containsKey("_MAX_TIME_RETAIN"))
			throw new Exception("_MAX_TIME_RETAIN name is missing");
		
		int _MAX_TIME_RETAIN = Integer.parseInt(config
				.get("_MAX_TIME_RETAIN"));

		cache = CacheBuilder.newBuilder().maximumSize(_MAX_CACHE_SIZE)
				.expireAfterWrite(_MAX_TIME_RETAIN, TimeUnit.MINUTES)
				.build();
		}
		catch(Exception e)
		{
			System.out.println("Could not initialize alerts adapter");
			e.printStackTrace();
			System.exit(0);
		}
	}

	@Override
	public boolean initialize() {
		return true;
	}

	@Override
	public boolean refresh() throws Exception {
		return true;
	}

	@Override
	public Map<String, JSONObject> alert(JSONObject raw_message) {
		
		System.out.println("LOOKING FOR ENRICHMENT TAG: " + enrichment_tag);

		Map<String, JSONObject> alerts = new HashMap<String, JSONObject>();
		JSONObject content = (JSONObject) raw_message.get("message");
		


		JSONObject enrichment = null;

		if (raw_message.containsKey("enrichment"))
			enrichment = (JSONObject) raw_message.get("enrichment");

		if (enrichment.containsKey(enrichment_tag)) {
			
			System.out.println("FOUND TAG: " + enrichment_tag);
			
			JSONObject cif = (JSONObject) enrichment.get(enrichment_tag);
			
			int cnt=0;
			
			for(Object key : cif.keySet())
			{
				JSONObject tmp = (JSONObject) cif.get(key);
				cnt = cnt + tmp.size();
			}
			
			if(cnt == 0)
			{
				System.out.println("TAG HAS NO ELEMENTS");
				return null;
			}
			
			JSONObject alert = new JSONObject();

			alert.put("title", "Flagged by CIF");
			alert.put("priority", "1");
			alert.put("type", "error");
			alert.put("designated_host", "Uknown");
			alert.put("source", "NA");
			alert.put("dest", "NA");
			alert.put("body", "Flagged by CIF");
			
			String source = "unknown";
			String dest = "unknown";
			
			if(content.containsKey("ip_src_addr"))
				source = content.get("ip_src_addr").toString();
			
			if(content.containsKey("ip_dst_addr"))
				dest = content.get("ip_dst_addr").toString();

			String alert_id = generateAlertId(source, dest, 0);

			alert.put("reference_id", alert_id);
			alerts.put(alert_id, alert);

			alert.put("enrichment", enrichment);

			return alerts;
		} else
		{
			System.out.println("DID NOT FIND TAG: " + enrichment_tag);
			return null;
		}

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
