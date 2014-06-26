/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opensoc.enrichments.geo.adapters;

import java.io.Serializable;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.json.simple.JSONObject;
import org.slf4j.Logger;

import com.opensoc.enrichment.interfaces.GeoAdapter;

public class GeoMysqlAdapter implements GeoAdapter, Serializable {

	private static final long serialVersionUID = 2675621052400811942L;
	private Logger _LOG;

	private Connection connection = null;
	private Statement statement = null;
	private ResultSet resultSet = null;

	@SuppressWarnings("unchecked")
	public String enrich(String metadata) {
		try {

			_LOG.debug("Received metadata: " + metadata);

			InetAddress addr = InetAddress.getByName(metadata);

			if (addr.isAnyLocalAddress() || addr.isLoopbackAddress()
					|| addr.isSiteLocalAddress() || addr.isMulticastAddress()) {
				_LOG.debug("Not a remote IP: " + metadata);
				_LOG.debug("Returning enrichment: " + "{}");

				return "{}";
			}

			_LOG.debug("Is a valid remote IP: " + metadata);

			statement = connection.createStatement();
			resultSet = statement.executeQuery("select IPTOLOCID(\"" + metadata
					+ "\") as ANS");

			resultSet.next();
			String locid = resultSet.getString("ANS");

			resultSet = statement
					.executeQuery("select * from location where locID = "
							+ locid + ";");
			resultSet.next();

			JSONObject jo = new JSONObject();
			jo.put("locID", resultSet.getString("locID"));
			jo.put("country", resultSet.getString("country"));
			jo.put("city", resultSet.getString("city"));
			jo.put("postalCode", resultSet.getString("postalCode"));
			jo.put("latitude", resultSet.getString("latitude"));
			jo.put("longitude", resultSet.getString("longitude"));
			jo.put("dmaCode", resultSet.getString("dmaCode"));
			jo.put("locID", resultSet.getString("locID"));

			String enrichment = jo.toJSONString();
			_LOG.debug("Returning enrichment: " + enrichment);

			return enrichment;

		} catch (Exception e) {
			e.printStackTrace();
			_LOG.error("Enrichment failure");
			return "{}";
		}
	}

	public boolean initializeAdapter(String ip, Logger LOG) {
		_LOG = LOG;

		_LOG.info("Initializing MysqlAdapter....");

		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://" + ip
					+ "/GEO?user=james&password=d3velop");

			_LOG.info("Set JDBC connection....");

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			_LOG.error("JDBC connection failed....");

			return false;
		}

	}
}
