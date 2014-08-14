package com.opensoc.dataservices.websocket;


import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket(maxTextMessageSize = 64 * 1024)
public class RandomMessageSenderSocket 
{
	boolean authGood = false;
	
	public RandomMessageSenderSocket( final boolean authGood )
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
		System.out.println( "WebSocket closed!" );
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
			System.out.println( "starting message generation loop!" );
			
			for( int i = 0; i < 100; i++ )
			{
				try 
				{
					String rand = UUID.randomUUID().toString();
					System.out.println( "sending: " + rand );
					session.getRemote().sendString( rand );
					
					try 
					{
						Thread.sleep( 2500 );
					} 
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					}
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}	
			}
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