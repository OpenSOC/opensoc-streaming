#OpenSOC-Alerts

##Module Description

This module enables telemetry alerts.  It splits the mssage stream into two streams.  The original message is emitted on the "message" stream.  The corresponding alert is emitted on the "alerts" stream.  The two are tied together through the alrts UUID.  

##Message Format

Assuming the original message (with enrichments enabled) has the following format:

```json
{
"message": 
{"ip_src_addr": xxxx, 
"ip_dst_addr": xxxx, 
"ip_src_port": xxxx, 
"ip_dst_port": xxxx, 
"protocol": xxxx, 
"additional-field 1": xxxx,
},
"enrichment" : {"geo": xxxx, "whois": xxxx, "hosts": xxxxx, "CIF": "xxxxx"}

}
```

The telemetry message will be tagged with a UUID alert tag like so:

```json
{
"message": 
{"ip_src_addr": xxxx, 
"ip_dst_addr": xxxx, 
"ip_src_port": xxxx, 
"ip_dst_port": xxxx, 
"protocol": xxxx, 
"additional-field 1": xxxx,
},
"enrichment" : {"geo": xxxx, "whois": xxxx, "hosts": xxxxx, "CIF": "xxxxx"},
"alerts": [UUID1, UUID2, UUID3, etc]

}
```

The alert will be fired on the "alerts" stream and can be customized to have any format.  The only requirement for the alert is the corresponding UUID tag to tie it back to the telemetry message 

```json
{
{
"alert": UUID
"content": xxxx

}
```

##Alerts Bolt

The bolt can be extended with a variety of alerts adapters.  The ability to stack alerts is currently in beta, but is not currently advisable.  We advice to only have one alerts bolt per topology.  The adapters are rules-based adapters which fire alerts when rules are a match.  Currently only Java adapters are provided, but there are future plans to provide Grok-Based adapters as well.

The signature of the Alerts bolt is as follows:

```
TelemetryAlertsBolt alerts_bolt = new TelemetryAlertsBolt()
.withIdentifier(alerts_identifier).withMaxCacheSize(1000)
.withMaxTimeRetain(3600).withAlertsAdapter(alerts_adapter)
.withMetricConfiguration(config);
```
Identifier - JSON key where the alert is attached
TimeRetain & MaxCacheSize - Caching parameters for the bolt
MetricConfiguration - export custom bolt metrics to graphite (if not null)
AlertsAdapter - pick the appropriate adapter for generating the alerts

### Java Adapters

Java adapters are designed for high volume topologies, but are not easily extensible.  The adapters provided are:

* com.opensoc.alerts.adapters.AllAlertsAdapter - will tag every single message with the static alert (appropriate for topologies like Sourcefire, etc, where every single message is an alert)
* com.opensoc.alerts.adapters.HbaseWhiteAndBlacklistAdapter - will read white and blacklists from HBase and fire alerts if source or dest IP are not on the whitelist or if any IP is on the blacklist

###Grok Adapters

Grok alerts adapters for OpenSOC are still under devleopment

###Stacking Alert Adapters

The functionality to stack alerts adapters is still under development
