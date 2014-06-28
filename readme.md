# OpenSOC-Streaming

Extensible set of Storm topologies and topology attributes for streaming, enriching, indexing, and storing telemetry in Hadoop.


## Build Instructions
To build the project in Eclipse do the following:

Check out via git plugin or git app
File->import->maven->exiting maven project

# Usage Instructions

## Message Parser Bolt

```
TelemetryParserBolt parser_bolt = new TelemetryParserBolt()
				.withMessageParser(new BasicSourcefireParser())
				.withOutputFieldName(topology_name);
```
				
###Parameters:

MesageParser: parsers a raw message to JSON
- BasicSourcefireParser: will parse a Sourcefire message to JSON
- BasicBroParser: will parser a bro message to JSON
