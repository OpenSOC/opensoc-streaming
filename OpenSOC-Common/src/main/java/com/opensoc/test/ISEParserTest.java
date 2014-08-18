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
		
		//String input2 = "k=k1=v1\\,k2=v2\\,k3=v3,Header=Aug  6 16:45:14 10.42.7.64 Aug  7 00:04:26 npf-sjca-pdp02 CISE_Passed_Authentications 0000370914 1 0 2014-08-07 00:04:26.299 -07:00 0011267964 5200 NOTICE Passed-Authentication: Authentication succeeded, ConfigVersionId=240, Device IP Address=10.56.129.4, DestinationIPAddress=10.42.7.64, DestinationPort=1812, UserName=host/ISTERN-WS02.cisco.com, Protocol=Radius, RequestLatency=84, NetworkDeviceName=NTN-WLC1, User-Name=host/ISTERN-WS02.cisco.com, NAS-IP-Address=10.56.129.4, NAS-Port=1, Service-Type=Framed, Framed-MTU=1300,Called-Station-ID=70-10-5c-f3-2f-80:alpha_byod, Calling-Station-ID=24-77-03-f9-4e-cc, NAS-Identifier=ntn01-11a-wlc1, NAS-Port-Type=Wireless - IEEE 802.11, Tunnel-Type= VLAN, Tunnel-Medium-Type= 802, Tunnel-Private-Group-ID= 604, undefined-89=";
		
		String input2 = 
				"head=Aug  6 16:46:16 10.42.7.63 Aug  7 00:05:28 npf-sjca-pdp01 CISE_Passed_Authentications 0001970226 1 0 2014-08-07 00:05:28.384 -07:00 0098666099 5200 NOTICE Passed-Authentication: Authentication succeeded, ConfigVersionId=133, Device IP Address=10.34.150.68, DestinationIPAddress=10.42.7.63, DestinationPort=1812, UserName=karganes, Protocol=Radius, RequestLatency=51, NetworkDeviceName=WNBU_NGWC_OTA_KATANA1, User-Name=karganes, NAS-IP-Address=10.34.150.68, NAS-Port=60000, Service-Type=Framed, Framed-IP-Address=10.34.137.144, Framed-MTU=1449, State=37CPMSessionID=0a22964453e2ae150000038a\\;42SessionID=npf-sjca-pdp01/195491152/2085484\\;, Called-Station-ID=18-33-9d-71-aa-40:alpha, Calling-Station-ID=48-F8-B3-7B-E6-7C, NAS-Port-Type=Wireless - IEEE 802.11, NAS-Port-Id=Capwap9, EAP-Key-Name=, cisco-av-pair=service-type=Framed, cisco-av-pair=audit-session-id=0a22964453e2ae150000038a, cisco-av-pair=method=dot1x, cisco-av-pair=cisco-wlan-ssid=alpha, Airespace-Wlan-Id=1, AcsSessionID=npf-sjca-pdp01/195491152/2085484, AuthenticationIdentityStore=CiscoAD, AuthenticationMethod=MSCHAPV2, SelectedAccessService=Default Network Access, SelectedAuthorizationProfiles=PermitAccess, Step=11001, Step=11017, Step=15049, Step=15008, Step=15048, Step=15048, Step=15048, Step=15048, Step=15048, Step=15004, Step=11507, Step=12300, Step=12625, Step=11006, Step=11001, Step=11018, Step=12302, Step=12318, Step=12800, Step=12805, Step=12806, Step=12807, Step=12810, Step=12305, Step=11006, Step=11001, Step=11018, Step=12304, Step=12305, Step=11006, Step=11001, Step=11018, Step=12304, Step=12305, Step=11006, Step=11001, Step=11018, Step=12304, Step=12305, Step=11006, Step=11001, Step=11018, Step=12304, Step=12318, Step=12812, Step=12804, Step=12801, Step=12802, Step=12816, Step=12310, Step=12305, Step=11006, Step=11001, Step=11018, Step=12304, Step=12313, Step=11521, Step=12305, Step=11006, Step=11001, Step=11018, Step=12304, Step=11522, Step=11806, Step=12305, Step=11006, Step=11001, Step=11018, Step=12304, Step=11808, Step=15041, Step=15006, Step=22072, Step=15013, Step=24430, Step=24325, Step=24313, Step=24319, Step=24367, Step=24367, Step=24367, Step=24367, Step=24367, Step=24367, Step=24367, Step=24367, Step=24367, Step=24323, Step=24343, Step=24402, Step=22037, Step=11824, Step=12305, Step=11006, Step=11001, Step=11018, Step=12304, Step=11810, Step=11814, Step=11519, Step=12314, Step=12305, Step=11006, Step=11001, Step=11018, Step=12304, Step=24423, Step=15036, Step=24432, Step=24355, Step=24416, Step=24355, Step=24420, Step=15048, Step=15048, Step=15048, Step=15048, Step=15048, Step=15004, Step=15016, Step=12306, Step=11503, Step=11002, SelectedAuthenticationIdentityStores=CiscoAD, SelectedAuthenticationIdentityStores=Internal Endpoints, SelectedAuthenticationIdentityStores=Internal Users, SelectedAuthenticationIdentityStores=Guest Users, NetworkDeviceGroups=Location#All Locations#SJC#WNBU, NetworkDeviceGroups=Device Type#All Device Types#Wireless#WLC#NGWC, AuthorizationPolicyMatchedRule=Default, EapTunnel=PEAP, EapAuthentication=EAP-MSCHAPv2, CPMSessionID=0a22964453e2ae150000038a, EndPointMACAddress=48-F8-B3-7B-E6-7C, PostureAssessmentStatus=NotApplicable, ISEPolicySetName=Building_SJC14_WNBU, AllowedProtocolMatchedRule=WNBU_SJC14_Wireless_Dot1x, IdentitySelectionMatchedRule=Default, AD-Domain=cisco.com, AD-User-Resolved-Identities=karganes@cisco.com, AD-User-Candidate-Identities=karganes@cisco.com, AD-User-Join-Point=CISCO.COM, StepData=4= DEVICE.Location, StepData=5= Radius.Called-Station-ID, StepData=6= Radius.Service-Type, StepData=7= Radius.NAS-Port-Type, StepData=8= Radius.NAS-IP-Address, StepData=9=WNBU_SJC14_Wireless_Dot1x, StepData=72=EAP_TLS_BYOD, StepData=73=CiscoAD, StepData=74=CiscoAD, StepData=75=karganes, StepData=76=cisco.com, StepData=77=cisco.com, StepData=78=icm.cisco.com\\,Domain trust direction is one-way, StepData=79=sea-alpha.cisco.com\\,Domain trust direction is one-way, StepData=80=partnet.cisco.com\\,Domain trust direction is one-way, StepData=81=IL.NDS.COM\\,Domain trust direction is one-way, StepData=82=UK.NDS.COM\\,Domain trust direction is one-way, StepData=83=SN.local\\,Domain trust direction is one-way, StepData=84=webex.local\\,Domain trust direction is one-way, StepData=85=in.nds.com\\,Domain trust direction is one-way, StepData=86=US.NDS.COM\\,Domain trust direction is one-way, StepData=88=karganes@cisco.com, StepData=89=CiscoAD, StepData=108=CiscoAD, StepData=109=cisco.com, StepData=110=CiscoAD, StepData=111=cisco.com, StepData=112=CiscoAD, StepData=113= CiscoAD.ExternalGroups, StepData=114= Radius.Service-Type, StepData=115= Radius.NAS-Port-Type, StepData=116= Session.Device-OS, StepData=117= Radius.Called-Station-ID, StepData=118=Default, AD-User-Resolved-DNs=CN=karganes\\,OU=Employees\\,OU=Cisco Users\\,DC=cisco\\,DC=com, AD-User-DNS-Domain=cisco.com, AD-Groups-Names=cisco.com/Users/Domain Users, AD-User-NetBios-Name=CISCO, Location=Location#All Locations#SJC#WNBU, Device Type=Device Type#All Device Types#Wireless#WLC#NGWC, IdentityAccessRestricted=false, Response={State=ReauthSession:0a22964453e2ae150000038a; Class=CACS:0a22964453e2ae150000038a:npf-sjca-pdp01/195491152/2085484; EAP-Key-Name=19:53:e3:25:39:51:ff:85:80:09:d9:ce:e7:3f:89:92:55:30:9d:84:40:eb:6e:34:ab:3d:81:06:b2:c2:cc:1f:dc:53:e3:25:38:f6:b9:5b:d3:6a:a5:9a:de:ed:4b:ad:c3:19:90:68:12:f5:e8:9b:1a:04:2f:76:24:3d:ce:4e:e5; MS-MPPE-Send-Key=****; MS-MPPE-Recv-Key=****; LicenseTypes=1; },"
				;
		
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
