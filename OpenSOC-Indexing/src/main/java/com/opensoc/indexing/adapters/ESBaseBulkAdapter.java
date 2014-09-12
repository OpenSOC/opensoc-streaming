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
	private int element_count;

	@Override
	public boolean initializeConnection(String ip, int port,
			String cluster_name, String index_name, String document_name,
			int bulk_size) {

		_LOG.trace("[OpenSOC] Initializing ESBulkAdapter...");

		try {

			_index_name = index_name;
			_document_name = document_name;

			_bulk_size = bulk_size - 1;

			element_count = 0;

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

	@SuppressWarnings("unchecked")
	@Override
	public boolean bulkIndex(JSONObject raw_message) {

		try {

			bulkRequest.add(client.prepareIndex(_index_name, _document_name)
					.setSource(raw_message));

			return doIndex();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean bulkIndex(String raw_message) {

		try {

			bulkRequest.add(client.prepareIndex(_index_name, _document_name)
					.setSource(raw_message));

			return doIndex();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean doIndex() {
		try {
			_LOG.trace("Adding to bulk load: element " + element_count
					+ " of bulk size " + _bulk_size);

			element_count++;
			
			_LOG.trace("[OpenSOC] COUNT + BULK SIZE: " + element_count + " " + _bulk_size);

			if (element_count == _bulk_size) {
				_LOG.trace("[OpenSOC] Starting bulk load of size: " + _bulk_size);
				BulkResponse resp = bulkRequest.execute().actionGet();
				element_count = 0;
				_LOG.trace("[OpenSOC] Received bulk response: " + resp.toString());
				
				_LOG.trace("[OpenSOC] SENDING BULK INGEST: " + element_count);

				if (resp.hasFailures()) {
					_LOG.error("[OpenSOC] Bulk update failed");
					throw new Exception("Bulk update failed at element_count: " + element_count);
				}
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
