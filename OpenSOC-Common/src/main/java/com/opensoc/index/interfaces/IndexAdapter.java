package com.opensoc.index.interfaces;

public interface IndexAdapter {

	boolean initializeConnection(String ip, int port, String cluster_name,
			String index_name, String document_name, int bulk);

	boolean bulkIndex(String raw_message);
}
