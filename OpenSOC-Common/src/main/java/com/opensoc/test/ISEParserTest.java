package com.opensoc.test;

import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.opensoc.ise.parser.ISEParser;
import com.opensoc.ise.parser.ParseException;

public class ISEParserTest {

	public static void main(String[] args) throws ParseException {

		//String input2 = "k=k1=v1\\,k2=v2\\,k3=v3,Header=Aug  6 16:45:14 10.42.7.64 Aug  7 00:04:26 npf-sjca-pdp02 CISE_Passed_Authentications 0000370914 1 0 2014-08-07 00:04:26.299 -07:00 0011267964 5200 NOTICE Passed-Authentication: Authentication succeeded, ConfigVersionId=240, Device IP Address=10.56.129.4, DestinationIPAddress=10.42.7.64, DestinationPort=1812, UserName=host/ISTERN-WS02.cisco.com, Protocol=Radius, RequestLatency=84, NetworkDeviceName=NTN-WLC1, User-Name=host/ISTERN-WS02.cisco.com, NAS-IP-Address=10.56.129.4, NAS-Port=1, Service-Type=Framed, Framed-MTU=1300,Called-Station-ID=70-10-5c-f3-2f-80:alpha_byod, Calling-Station-ID=24-77-03-f9-4e-cc, NAS-Identifier=ntn01-11a-wlc1, NAS-Port-Type=Wireless - IEEE 802.11, Tunnel-Type= VLAN, Tunnel-Medium-Type= 802, Tunnel-Private-Group-ID= 604, undefined-89=";
		
		String input2 = "k=k1=v1\\,k2=v2\\,k3=v3,Header=Aug  6 16:45:14 10.42.7.64 Aug  7 00:04:26 npf-sjca-pdp02 CISE_Passed_Authentications 0000370914 1 0 2014-08-07 00:04:26.299 -07:00 0011267964 5200 NOTICE Passed-Authentication: Authentication succeeded, ConfigVersionId=240, Device IP Address=10.56.129.4, DestinationIPAddress=10.42.7.64, DestinationPort=1812, UserName=host/ISTERN-WS02.cisco.com, Protocol=Radius, RequestLatency=84, NetworkDeviceName=NTN-WLC1, User-Name=host/ISTERN-WS02.cisco.com, NAS-IP-Address=10.56.129.4, NAS-Port=1, Service-Type=Framed, Framed-MTU=1300,Called-Station-ID=70-10-5c-f3-2f-80:alpha_byod, Calling-Station-ID=24-77-03-f9-4e-cc, NAS-Identifier=ntn01-11a-wlc1, NAS-Port-Type=Wireless - IEEE 802.11, Tunnel-Type= VLAN, Tunnel-Medium-Type= 802, Tunnel-Private-Group-ID= 604, undefined-89=";
		
		//String input2 = "k=k1=v1\\,k2=v2\\,k2=v2,k3=v3";
		System.out.println(input2);
		ISEParser parser = new ISEParser(new StringReader(input2));

		Map result = parser.parseObject();

		System.out.println(result.size());

		Iterator it = result.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<String, String> entry = (Entry<String, String>) it.next();
			System.out.println(entry.getKey().trim() + "---" + entry.getValue());
		}

	}

}
