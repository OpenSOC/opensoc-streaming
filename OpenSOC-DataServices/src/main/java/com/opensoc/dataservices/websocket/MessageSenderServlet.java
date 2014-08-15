package com.opensoc.dataservices.websocket;

import javax.servlet.annotation.WebServlet;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import com.google.inject.Inject;

@WebServlet(name = "Message Sender Servlet", urlPatterns = { "/messages" })
public class MessageSenderServlet extends WebSocketServlet
{	
	@Inject
	private MyWebSocketCreator socketCreator;
	
	@Override
	public void configure(WebSocketServletFactory factory) 
	{
		factory.getPolicy().setIdleTimeout(600000);
		factory.setCreator( socketCreator );
	}
}