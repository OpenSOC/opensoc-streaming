package com.opensoc.alerts.adapters;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.opensoc.alerts.interfaces.AlertsAdapter;

public class CIFAlertsAdapter implements AlertsAdapter, Serializable {
	
	public CIFAlertsAdapter(Map<String, String> configs)
	{
		
	}

	@Override
	public boolean initialize() {
		return false;
	}

	@Override
	public boolean refresh() throws Exception {
		return false;
	}

	@Override
	public Map<String, JSONObject> alert(JSONObject raw_message) {

		Map<String, JSONObject> alerts = new HashMap<String, JSONObject>();

		JSONObject enrichment = null;

		if (raw_message.containsKey("enrichment"))
			enrichment = (JSONObject) raw_message.get("enrichment");

		if (enrichment.containsKey("cif")) {

			JSONObject alert = new JSONObject();

			alert.put("title", "Flagged by CIF");
			alert.put("priority", "1");
			alert.put("type", "error");
			alert.put("designated_host", "Uknown");
			alert.put("source", "NA");
			alert.put("dest", "NA");
			alert.put("body", "Flagged by CIF");

			String alert_id = UUID.randomUUID().toString();

			alert.put("reference_id", alert_id);
			alerts.put(alert_id, alert);

			alert.put("enrichment", enrichment);

			return alerts;
		} else
			return null;

	}

	@Override
	public boolean containsAlertId(String alert) {
		// TODO Auto-generated method stub
		return false;
	}

}
