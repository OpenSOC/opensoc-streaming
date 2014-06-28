# OpenSOC-Streaming

Extensible set of Storm topologies and topology attributes for streaming, enriching, indexing, and storing telemetry in Hadoop.


## Build Instructions
To build the project in Eclipse do the following:

Check out via git plugin or git app
File->import->maven->exiting maven project

# Usage Instructions

## Message Parser Bolt

Bolt for parsing telemetry messages into a JSON format

```
TelemetryParserBolt parser_bolt = new TelemetryParserBolt()
				.withMessageParser(new BasicSourcefireParser())
				.withOutputFieldName(topology_name);
```
				
###Parameters:

MesageParser: parsers a raw message to JSON. Parsers listed below are available
- BasicSourcefireParser: will parse a Sourcefire message to JSON
- BasicBroParser: will parser a bro message to JSON

OutputFieldName: name of the output field emitted by the bolt

## Telemetry Indexing Bolt

Bolt for indexing JSON telemetry messages in ElasticSearch or Solr

```
TelemetryIndexingBolt indexing_bolt = new TelemetryIndexingBolt()
				.withIndexIP(ElasticSearchIP).withIndexPort(elasticSearchPort)
				.withClusterName(ElasticSearchClusterName)
				.withIndexName(ElasticSearchIndexName)
				.withDocumentName(ElasticSearchDocumentName).withBulk(bulk)
				.withOutputFieldName(topology_name)
				.withIndexAdapter(new ESBaseBulkAdapter());
```

###Parameters:

-IndexAdapter: adapter and strategy for indexing.  Adapters listed below are available
-- ESBaseBulkAdapter: adapter for bulk loading telemetry into a single index in ElasticSearch
-- ESBulkRotatingAdapter: adapter for bulk loading telemetry into Elastic search, rotating once per hour, and applying a single alias to all rotated indexes
-- SolrAdapter (stubbed out, on roadmap)

OutputFieldName: name of the output field emitted by the bolt

IndexIP: IP of ElasticSearch/Solr

IndexPort: Port of ElasticSearch/Solr

ClusterName: ClusterName of ElasticSearch/Solr

IndexName: IndexName of ElasticSearch/Solr

DocumentName: DocumentName of ElasticSearch/Solr

Bulk: number of documents to bulk load into ElasticSearch/Solr.  If no value is passed, default is 10

