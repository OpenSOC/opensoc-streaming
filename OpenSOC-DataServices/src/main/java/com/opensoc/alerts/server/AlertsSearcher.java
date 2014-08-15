package com.opensoc.alerts.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.function.Consumer;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import com.google.inject.Inject;

public class AlertsSearcher implements Runnable {

	@Inject
	private Properties configProps;
	
	// TODO: inject Searcher module for either ElasticSearch or Solr...
	// TODO: inject OpenSocServiceFactory here
	
	@Override
	public void run() {

		try
		{
			System.out.println( "Doing Elastic Search search..." );
			
			long currentSearchTime = System.currentTimeMillis();
			long lastSearchTime = 0L;
			
			// look for a marker that tells us the last time we ran...
			String homeDir = configProps.getProperty("homeDir");
			if( homeDir.endsWith( "/" )) {
				homeDir = homeDir.substring(0, homeDir.length()-1);
			}
			
			System.out.println( "using homeDir = " + homeDir );
			
			File searcherStateFile = new File( homeDir + "/searcherState.properties" );
			if( searcherStateFile.exists() )
			{
				System.out.println( "found existing searcherState.properties file" );
				FileInputStream fis = null;
				try {
					fis = new FileInputStream( searcherStateFile );
					Properties searcherState = new Properties();
					searcherState.load(fis);
					lastSearchTime = Long.parseLong( searcherState.getProperty("lastSearchTime"));
				}
				catch( FileNotFoundException e ) {
					e.printStackTrace();
					// TODO: handle this exception properly...
				} catch (IOException e) {
					// TODO: handle this exception properly...
					e.printStackTrace();
				}
				finally
				{
					try {
						fis.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			else
			{
				// nothing to do here.  We'll write out our lastSearchTime at the end
				System.out.println( "No existing searcherState.properties found" );
			}
			
			// search for alerts newer than "lastSearchTime" 
			Settings settings = ImmutableSettings.settingsBuilder()
					.put("client.transport.sniff", true).build();
			        // .put("cluster.name", "elasticsearch").build();
	
			Client client = null;
			try
			{
				System.out.println( "initializing elasticsearch client" );
				
				String elasticSearchHostName = configProps.getProperty( "elasticSearchHostName", "localhost" );
				int elasticSearchHostPort = Integer.parseInt(configProps.getProperty( "elasticSearchHostPort", "9300" ) );
				client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(elasticSearchHostName, elasticSearchHostPort));
					
				System.out.println( "lastSearchTime: " + lastSearchTime );
				
				SearchResponse response = client.prepareSearch( "alerts" )
				.setTypes( "alert" )
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.addField("_source")
				// .setQuery(QueryBuilders.queryString("enrichment.geo.ip_src_addr.country:US"))		
				.setQuery( QueryBuilders.boolQuery().must(  QueryBuilders.wildcardQuery( "alert.source", "*" ) )
													.must( QueryBuilders.rangeQuery("message.timestamp").from(lastSearchTime).to(System.currentTimeMillis()).includeLower(true).includeUpper(false)))
				.execute()
				.actionGet();
				
				SearchHits hits = response.getHits();
				System.out.println( "Total hits: " + hits.getTotalHits());

				
				// for all hits, put the alert onto the Kafka topic.
				Consumer<SearchHit> func =  this::doSenderWork;
				hits.forEach( func );
			
			}
			finally
			{			
				if( client != null )
				{
					client.close();
				}
			}		
			
			// record the time we just searched
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream( searcherStateFile );
				Properties searcherState = new Properties();
				searcherState.setProperty( "lastSearchTime", Long.toString(currentSearchTime));
				searcherState.store(fos, "");
				
			}
			catch(  FileNotFoundException e ) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally {
				
				try {
					fos.close();
				} 
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			System.out.println( "Done with ElasticSearch search... " );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	private void doSenderWork( SearchHit hit )
	{	
		String kafkaBrokerHostName = configProps.getProperty("kafkaBrokerHostName", "localhost" );
		String kafkaBrokerHostPort = configProps.getProperty("kafkaBrokerHostPort", "9092" );
		String kafkaTopicName = configProps.getProperty("kafkaTopicName", "test" );
		
		System.out.println( "kafkaBrokerHostName: " + kafkaBrokerHostName );
		System.out.println( "kafkaBrokerHostPort: " + kafkaBrokerHostPort );
		System.out.println( "kafkaTopicName: " + kafkaTopicName );
		
		String sourceData = hit.getSourceAsString();
		
		System.out.println( "Source Data: " + sourceData );
		Properties props = new Properties();
		 
		props.put("metadata.broker.list", kafkaBrokerHostName + ":" + kafkaBrokerHostPort );
		props.put("serializer.class", "kafka.serializer.StringEncoder");
		// props.put("partitioner.class", "example.producer.SimplePartitioner");
		props.put("request.required.acks", "1");
		 
		ProducerConfig config = new ProducerConfig(props);
		
		Producer<String, String> producer = new Producer<String, String>(config);
		
		KeyedMessage<String, String> data = new KeyedMessage<String, String>(kafkaTopicName, "", sourceData );
		 
		producer.send(data);		
	}
}