package com.opensoc.index.interfaces;

import org.slf4j.Logger;

public interface IndexAdapter {

	boolean initializeConnection(String ip, int port, String cluster_name,
			String index_name, String document_name, int bulk, Logger LOG);

	boolean bulkIndex(String raw_message);
}
