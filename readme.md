# OpenSOC-Streaming

Extensible set of Storm topologies and topology attributes for streaming, enriching, indexing, and storing telemetry in Hadoop.  Please see the wiki for more detailed documentation. For general information on the project see our slides on www.getopensoc.com


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
- BasicBroParser: will parse a Bro message to JSON

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

IndexAdapter: adapter and strategy for indexing.  Adapters listed below are available
- ESBaseBulkAdapter: adapter for bulk loading telemetry into a single index in ElasticSearch
- ESBulkRotatingAdapter: adapter for bulk loading telemetry into Elastic search, rotating once per hour, and applying a single alias to all rotated indexes
- SolrAdapter (stubbed out, on roadmap)

OutputFieldName: name of the output field emitted by the bolt

IndexIP: IP of ElasticSearch/Solr

IndexPort: Port of ElasticSearch/Solr

ClusterName: ClusterName of ElasticSearch/Solr

IndexName: IndexName of ElasticSearch/Solr

DocumentName: DocumentName of ElasticSearch/Solr

Bulk: number of documents to bulk load into ElasticSearch/Solr.  If no value is passed, default is 10

## Geo Enrichment Bolt

Bolt for enriching IPs with geo data from the MaxMind GeoLite database. This database must be setup prior to using the bolt. Please see the wiki entry on how to setup the database: https://github.com/OpenSOC/opensoc-streaming/wiki/Setting-up-GeoLite-Data.

There are two flavors of GeoEnrichment bolts: Single and Dual.  The single bolt enriches a single IP in a message and a dual bolt enriches an IP pair (source and dest).

This is how to invoke the dual bolt:

```
DualGeoEnrichmentBolt geo_enrichment = new DualGeoEnrichmentBolt()
				.withEnrichmentSourceIP(geo_enrichment_source_ip)
				.withSurceIpRegex(originator_ip_regex)
				.withDestIpRegex(responder_ip_regex)
				.withEnrichmentTag(geo_enrichment_tag)
				.withOutputFieldName(topology_name)
				.withGeoAdapter(new GeoMysqlAdapter())
				.withMaxTimeRetain(MAX_TIME_RETAIN)
				.withMaxCacheSize(MAX_CACHE_SIZE);
```

###Parameters:

GeoAdapter: adapter for the MaxMind GeoLite dataset.  Adapters listed below are available
- GeoMysqlAdapter: pulls geoIP data from MqSQL database
- GeoPosgreSQLAdapter: pulls geoIP data from Posgress database (on road map, not yet available)

originator_ip_regex: regex to extract the source ip form message

responder_ip_regex: regex to extract dest ip from message
The single bolt is currently undergoing testing and will be uploaded shortly

geo_enrichment_tag: JSON field indicating how to tag the original message with the enrichment... {original_message:some_message, {geo_enrichment_tag:{from:xxx},{to:xxx}}}

MAX_TIME_RETAIN: this bolt utilizes in-memory cache. this variable (in minutes) indicates now long to retain each entry in the cache

MAX_CACHE_SIZE: this value defines the maximum size of the cache after which entries are evicted from cache

OutputFieldName: name of the output field emitted by the bolt

