package com.opensoc.dataservices.websocket;

import java.net.HttpCookie;
import java.util.List;
import java.util.Properties;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.opensoc.dataservices.kafkaclient.KafkaConsumer;

public class KafkaWebSocketCreator implements WebSocketCreator
{
	private static final Logger logger = LoggerFactory.getLogger( KafkaWebSocketCreator.class );
	
	@Inject
	private Properties configProps;
	
	@Override
	public Object createWebSocket(ServletUpgradeRequest request, ServletUpgradeResponse response) 
	{
		boolean authGood = false;
		List<HttpCookie> cookies = request.getCookies();
		for( HttpCookie cookie : cookies )
		{
			String name = cookie.getName();
			if( name!= null && name.equals( "authToken" ))
			{
				String value = cookie.getValue();
				if( value != null && value.equals( "ABC123" ))
				{
					authGood = true;
					break;
				}
				
			}
			else
			{
				continue;
			}
		}
		
		// return new RandomMessageSenderSocket( authGood );
		return new KafkaMessageSenderSocket( configProps, authGood );
	}
}