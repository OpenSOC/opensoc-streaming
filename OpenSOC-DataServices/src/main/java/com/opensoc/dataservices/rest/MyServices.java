package com.opensoc.dataservices.rest;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;

public class MyServices extends Application 
{
	private static Set services = new HashSet();

	public MyServices() 
	{
		// initialize restful services
		services.add(new Index());
	}

	@Override
	public Set getSingletons() 
	{
		return services;
	}

	public static Set getServices() 
	{
		return services;
	}
}