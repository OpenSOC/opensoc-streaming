package com.opensoc.pcapservice.rest;

import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;


public class PcapService {

	public static void main(String[] args) throws IOException {

		//PcapServiceImpl.setConstants(args[0], Integer.parseInt(args[1]));
		Server server = new Server(2014);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		ServletHolder h = new ServletHolder(new HttpServletDispatcher());
		h.setInitParameter("javax.ws.rs.Application", "com.opensoc.pcapservice.rest.PcapServiceImplRestEasy");
		context.addServlet(h, "/*");
		server.setHandler(context);
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}