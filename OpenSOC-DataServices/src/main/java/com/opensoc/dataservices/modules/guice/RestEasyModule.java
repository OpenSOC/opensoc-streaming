package com.opensoc.dataservices.modules.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.opensoc.dataservices.rest.Index;

public class RestEasyModule extends AbstractModule {
	
	@Override
	protected void configure() {
		
		bind( Index.class );
	}
}
