package com.opensoc.dataservices;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.opensoc.dataservices.common.ClientErrorExceptionMapper;
import com.opensoc.dataservices.common.GsonMessageBodyHandler;
import com.opensoc.dataservices.common.OpenSOCService;
import com.opensoc.dataservices.common.UserObject;
import com.opensoc.service.wrappers.AlertsServiceWrapper;
import com.opensoc.services.alerts.ElasticSearch_KafkaAlertsService;
import com.opensoc.services.alerts.Solr_KafkaAlertsService;

import javax.inject.Singleton;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.guice.RequestScoped;
import org.jboss.resteasy.plugins.guice.ext.RequestScopeModule;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

public class Main {
	
	static int port = 8080;

    public static void main(String[] args) throws Exception {
        Injector injector = Guice.createInjector(new ServiceModule(args));

        injector.getAllBindings();

        injector.createChildInjector().getAllBindings();

        Server server = new Server(port);
        ServletContextHandler servletHandler = new ServletContextHandler();
        servletHandler.addEventListener(injector.getInstance(GuiceResteasyBootstrapServletContextListener.class));

        ServletHolder sh = new ServletHolder(HttpServletDispatcher.class);
        servletHandler.setInitParameter("resteasy.role.based.security", "true");
        servletHandler.addFilter(new FilterHolder(injector.getInstance(RequestFilter.class)), "/*", null);
        //servletHandler.addServlet(DefaultServlet.class, "/*");
        servletHandler.addServlet(sh, "/*");

        server.setHandler(servletHandler);
        server.start();
        server.join();
    }

    private static class ServiceModule extends RequestScopeModule {

        private String[] args;

        public ServiceModule(String[] args) {
            this.args = args;
        }

        @Provides
        @Singleton
        public OpenSOCService socservice() {
            if (args.length > 0 && args[0].equals("ElasticSearch_KafkaAlertsService")) {
                return new ElasticSearch_KafkaAlertsService();
            } else {
                return new Solr_KafkaAlertsService();
            }
        }

        @Override
        protected void configure() {
            super.configure();
            bind(GsonMessageBodyHandler.class);
            bind(AlertsServiceWrapper.class);
            bind(ClientErrorExceptionMapper.class);
        }

        @Provides
        @RequestScoped
        public UserObject provideUser() {
            return ResteasyProviderFactory.getContextData(UserObject.class);
        }
    }
}
