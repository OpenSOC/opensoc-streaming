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

package com.opensoc.enrichment.adapters.geo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.Executors;

import org.json.simple.JSONObject;

@SuppressWarnings("serial")
public class GeoMysqlAdapter extends AbstractGeoAdapter {

	private Connection connection = null;
	private Statement statement = null;
	private ResultSet resultSet = null;

	private String _ip;
	private String _username;
	private String _password;
	private String _tablename;

	public GeoMysqlAdapter(String ip, int port, String username,
			String password, String tablename) {
		try {
			_ip = InetAddress.getByName(ip).getHostAddress();
			
			boolean reachable = InetAddress.getByName(ip).isReachable(100);

			if (!reachable)
				throw new Exception("Unable to reach IP " + _ip);
			
		} catch (Exception e) {
			_LOG.error("Environment misconfigured, cannot reach MYSQL server....");
			e.printStackTrace();
		}
		_username = username;
		_password = password;
		_tablename = tablename;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject enrich(String metadata) {
		try {

			_LOG.debug("Received metadata: " + metadata);

			InetAddress addr = InetAddress.getByName(metadata);

			if (addr.isAnyLocalAddress() || addr.isLoopbackAddress()
					|| addr.isSiteLocalAddress() || addr.isMulticastAddress()) {
				_LOG.debug("Not a remote IP: " + metadata);
				_LOG.debug("Returning enrichment: " + "{}");

				return new JSONObject();
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

			_LOG.debug("Returning enrichment: " + jo);

			return jo;

		} catch (Exception e) {
			e.printStackTrace();
			_LOG.error("Enrichment failure");
			return new JSONObject();
		}
	}

	@Override
	public boolean initializeAdapter() {

		_LOG.info("Initializing MysqlAdapter....");

		try {

			

			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://" + _ip
					+ "/" + _tablename + "?user=" + _username + "&password="
					+ _password);

			connection.setReadOnly(true);

			if (!connection.isValid(0))
				throw new Exception("Invalid connection string....");

			_LOG.info("Set JDBC connection....");

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			_LOG.error("JDBC connection failed....");

			return false;
		}

	}
}
