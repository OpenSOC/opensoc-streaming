package com.opensoc.alerts.adapters;

import java.util.Set;

import org.apache.commons.net.util.SubnetUtils;

public class RangeChecker {

	static boolean checkRange(Set<String> CIDR_networks, String ip) {
		boolean in_range = false;
		try {
			for (String network : CIDR_networks) {
				
				System.out.println("Looking at range: " + network + " and ip " + ip);
				SubnetUtils utils = new SubnetUtils(network);
				in_range = utils.getInfo().isInRange(ip);

				if (!in_range)
					return false;
			}
		} catch (Exception e) {
			return false;
		}

		return true;
	}
}
