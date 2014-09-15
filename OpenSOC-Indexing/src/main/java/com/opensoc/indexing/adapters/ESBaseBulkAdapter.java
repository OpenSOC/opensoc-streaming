package com.opensoc.indexing.adapters;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.json.simple.JSONObject;

@SuppressWarnings("serial")
public class ESBaseBulkAdapter extends AbstractIndexAdapter {

	private Client client;
	private BulkRequestBuilder bulkRequest;
	private int _bulk_size;
	private String _index_name;
	private String _document_name;

	@Override
	public boolean initializeConnection(String ip, int port,
			String cluster_name, String index_name, String document_name,
			int bulk_size) {

		_LOG.trace("[OpenSOC] Initializing ESBulkAdapter...");

		try {

			_index_name = index_name;
			_document_name = document_name;

			_bulk_size = bulk_size;

			System.out.println("Bulk indexing is set to: " + _bulk_size);

			Settings settings = ImmutableSettings.settingsBuilder()
					.put("cluster.name", cluster_name).build();
			client = new TransportClient(settings)
					.addTransportAddress(new InetSocketTransportAddress(ip,
							port));

			bulkRequest = client.prepareBulk();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}


	/**
	 * @param raw_message message to bulk index in Elastic Search 
	 * @return integer (0) loaded into a bulk queue, (1) bulk indexing executed, (2) error
	 */
	@SuppressWarnings("unchecked")
	public int bulkIndex(JSONObject raw_message) {

		boolean success = true;
		
		try {

			synchronized(bulkRequest)
			{

				bulkRequest.add(client.prepareIndex(_index_name, _document_name).setSource(raw_message));
				
				System.out.println("Number of actions is: " + bulkRequest.numberOfActions());
				
				if(bulkRequest.numberOfActions() == _bulk_size)
				{
					 success = doIndex();
					 bulkRequest = client.prepareBulk();
					 
					 if(success)
						 return 1;
					 else
						 return 2;
				}

			}
			
			return 0;
			
		} catch (Exception e) {
			e.printStackTrace();
			return 2;
		}
	}


	public boolean doIndex() {
		try {

			System.out.println("Performing bulk load of size: " + bulkRequest.numberOfActions());

				BulkResponse resp = bulkRequest.execute().actionGet();
				
				_LOG.trace("[OpenSOC] Received bulk response: " + resp.toString());
				


				if (resp.hasFailures()) {
					_LOG.error("[OpenSOC] Bulk update failed");
					throw new Exception("Bulk update failed at element_count: " + bulkRequest.numberOfActions() + " and response is: " + resp.toString());
				}
					
	
			return true;
		}
				
		 catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
