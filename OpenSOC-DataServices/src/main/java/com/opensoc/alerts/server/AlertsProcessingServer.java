package com.opensoc.alerts.server;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import com.google.inject.Inject;

public class AlertsProcessingServer {
	
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	// inject our AlertsSearcher runnable...
	@Inject
	private AlertsSearcher searcher;
	@Inject
	private Properties configProps;
	
	public void startProcessing() {
		
		System.out.println( "startProcessing() invoked" );
		
		int initialDelayTime = Integer.parseInt( configProps.getProperty( "searchInitialDelayTime", "30" ) );
		int searchIntervalTime = Integer.parseInt( configProps.getProperty( "searchIntervalTime", "30" ) );
		
		final ScheduledFuture<?> alertsSearcherHandle =
			       scheduler.scheduleAtFixedRate( searcher, initialDelayTime, searchIntervalTime, SECONDS );
		
	}
}