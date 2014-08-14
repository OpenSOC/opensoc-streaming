package com.opensoc.dataservices.websocket;

import javax.servlet.annotation.WebServlet;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

@WebServlet(name = "Message Sender Servlet", urlPatterns = { "/messages" })
public class MessageSenderServlet extends WebSocketServlet
{	
	@Override
	public void configure(WebSocketServletFactory factory) 
	{
		factory.getPolicy().setIdleTimeout(600000);
		factory.setCreator( new MyWebSocketCreator() );
	}
}
