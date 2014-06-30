# OpenSOC-Streaming

Extensible set of Storm topologies and topology attributes for streaming, enriching, indexing, and storing telemetry in Hadoop.  General information on OpenSOC is available at www.getopensoc.com

For OpenSOC FAQ please read the following wiki entry:  https://github.com/OpenSOC/opensoc-streaming/wiki/OpenSOC-FAQ


## Build Instructions

If building via maven add the following dependency:

```
<dependency>
	<groupId>com.opensoc</groupId>
	<artifactId>OpenSOC-Streaming</artifactId>
	<version>${opensoc.streaming.version}</version>
</dependency>
```

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

## Whois Enrichment Bolt

Bolt for enriching IPs with whois data. This data is available from a variety of sources, but is not free.  At Cisco we have our own internal data store of this data.  We maintain a list of sources where you can obtain whois data in the following wiki entry: https://github.com/OpenSOC/opensoc-streaming/wiki/Obtaining-Whois-data
This data must be stored into a ke-value store in order to be accessible the bolt.  As of release only HBase stores are supported.  Information on how to transform and load this data into HBase is available in the following wiki entry: https://github.com/OpenSOC/opensoc-streaming/wiki/Setting-up-Whois-Data

There are two flavors of WhoisEnrichment bolts: Single and Dual.  The single bolt enriches a single IP in a message and a dual bolt enriches an IP pair (source and dest).

This is how to invoke the dual bolt:

```
DualWhoisEnrichmentBolt geo_enrichment = new DualWhoisEnrichmentBolt()
				.withEnrichmentSourceIP(geo_enrichment_source_ip)
				.withSurceIpRegex(originator_ip_regex)
				.withDestIpRegex(responder_ip_regex)
				.withEnrichmentTag(geo_enrichment_tag)
				.withOutputFieldName(topology_name)
				.withGeoAdapter(new WhoisHBaseAdapter())
				.withMaxTimeRetain(MAX_TIME_RETAIN)
				.withMaxCacheSize(MAX_CACHE_SIZE);
```

###Parameters:

WhoisAdapter: adapter for whois database.  Adapters listed below are available
- WhoisHBaseAdapter: adapter for HBase

originator_ip_regex: regex to extract the source ip form message

responder_ip_regex: regex to extract dest ip from message
The single bolt is currently undergoing testing and will be uploaded shortly

geo_enrichment_tag: JSON field indicating how to tag the original message with the enrichment... {original_message:some_message, {geo_enrichment_tag:{from:xxx},{to:xxx}}}

MAX_TIME_RETAIN: this bolt utilizes in-memory cache. this variable (in minutes) indicates now long to retain each entry in the cache

MAX_CACHE_SIZE: this value defines the maximum size of the cache after which entries are evicted from cache

OutputFieldName: name of the output field emitted by the bolt

## CIF Enrichment Bolt

Bolt for enriching telemetry with the information from CIF threat intelligence feeds.  In order to use the bolt you must first integrate CIF with HBase or another key-value store to make the data available for access via the bolt.  As of release only HBase stores are supported.  The instructions for setting this up are provided in the following wiki entry: https://github.com/OpenSOC/opensoc-streaming/wiki/Setting-up-CIF-Data

Hortonworks to finish documentation

```
CIF bolt signature

```
###Parameters

Hortonworks to finish documentation

##Lancope Enrichment Bolt

Bolt for enriching telemetry with the information from Lancope.  In order to use the bolt you must first setup Lancope data in HBase.  or another key-value store to make the data available for access via the bolt.  As of release only HBase stores are supported.   The instructions for setting this up are provided in the following wiki entry: https://github.com/OpenSOC/opensoc-streaming/wiki/Setting-up-Lancope-data

Hortonworks to finish documentation

```
Lancope bolt signature

```

###Parameters

Hortonworks to finish documentation
