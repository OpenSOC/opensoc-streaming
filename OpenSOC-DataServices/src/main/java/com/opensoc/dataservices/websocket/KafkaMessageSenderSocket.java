package com.opensoc.dataservices.websocket;


import java.io.IOException;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.opensoc.dataservices.kafkaclient.KafkaClient;

@WebSocket(maxTextMessageSize = 64 * 1024)
public class KafkaMessageSenderSocket 
{
	boolean authGood = false;
	int threads = 1;
	KafkaClient client;
	String zooKeeper = "ec2-54-210-207-24.compute-1.amazonaws.com:2181";
    String groupId = "123f";
    String topic = "test";
	
    public KafkaMessageSenderSocket( final boolean authGood )
	{
		this.authGood = authGood;  
	}
	
	@OnWebSocketConnect
	public void onConnect( Session session )
	{
		System.out.println( "WebSocket connected!" );
	}
	
	@OnWebSocketClose
	public void onClose( Session session, int closeCode, String closeReason )
	{
		System.out.println( "WebSocket closed.  Shutting down Kafka client." );
		client.shutdown();
	}

	@OnWebSocketMessage
	public void onMessage( Session session, String text )
	{	
		
		if( !authGood )
		{
			try 
			{
				session.getRemote().sendString( "Must login first" );
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
			
			return;
		}
		
		System.out.println( "WebSocket TEXT message received: " + text );
		
		if( text.trim().equals( "startMessages" ))
		{
			client = new KafkaClient(zooKeeper, groupId, topic, session.getRemote() );
	        client.run(threads);
		}
		else if( text.trim().equals( "stopMessages" ))
		{
			client.shutdown();
		}
		else
		{
			System.out.println("Something bogus happened!" );
		}
	}
	
	@OnWebSocketMessage
	public void onMessage( Session session, byte[] buff, int offset, int length )
	{
		System.out.println( "WebSocket BINARY message received!" );
	}
}