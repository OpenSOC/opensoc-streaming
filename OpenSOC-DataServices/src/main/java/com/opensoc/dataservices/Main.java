package com.opensoc.dataservices;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;

import org.apache.jasper.servlet.JspServlet;
import org.apache.shiro.guice.web.ShiroWebModule;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.apache.shiro.realm.ldap.JndiLdapRealm;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import com.opensoc.dataservices.servlet.LoginServlet;
import com.opensoc.dataservices.servlet.LogoutServlet;
import com.opensoc.dataservices.websocket.MessageSenderServlet;

public class Main {
	
	static int port = 9091;
	
	private static final String WEBROOT_INDEX = "/webroot/";
	
    public static void main(String[] args) throws Exception {

        WebAppContext context = new WebAppContext();
    	
    	Injector injector = Guice.createInjector(new DefaultServletModule(), new DefaultShiroWebModule(context.getServletContext()), new AbstractModule() {
			
			@Override
			protected void configure() {
				binder().requireExplicitBindings();
				bind(GuiceFilter.class);
				bind( GuiceResteasyBootstrapServletContextListener.class );
				bind( EnvironmentLoaderListener.class );
				
			}
		});

    	
        injector.getAllBindings();
        injector.createChildInjector().getAllBindings();

        Server server = new Server(port);

        FilterHolder guiceFilter = new FilterHolder(injector.getInstance(GuiceFilter.class));
       

        
        
        /** For JSP support.  Used only for testing and debugging for now.  This came come out
         * once the real consumers for this service are in place
         */
        URL indexUri = Main.class.getResource(WEBROOT_INDEX);
        if (indexUri == null)
        {
            throw new FileNotFoundException("Unable to find resource " + WEBROOT_INDEX);
        }

        // Points to wherever /webroot/ (the resource) is
        URI baseUri = indexUri.toURI();        
        
        // Establish Scratch directory for the servlet context (used by JSP compilation)
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File scratchDir = new File(tempDir.toString(),"embedded-jetty-jsp");

        if (!scratchDir.exists())
        {
            if (!scratchDir.mkdirs())
            {
                throw new IOException("Unable to create scratch directory: " + scratchDir);
            }
        }        
        
        // Set JSP to use Standard JavaC always
        System.setProperty("org.apache.jasper.compiler.disablejsr199","false");	        
        
        context.setAttribute("javax.servlet.context.tempdir",scratchDir);
        context.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
        
        //Ensure the jsp engine is initialized correctly
        JettyJasperInitializer sci = new JettyJasperInitializer();
        ServletContainerInitializersStarter sciStarter = new ServletContainerInitializersStarter(context);
        ContainerInitializer initializer = new ContainerInitializer(sci, null);
        List<ContainerInitializer> initializers = new ArrayList<ContainerInitializer>();
        initializers.add(initializer);

        context.setAttribute("org.eclipse.jetty.containerInitializers", initializers);
        context.addBean(sciStarter, true);        
        
        // Set Classloader of Context to be sane (needed for JSTL)
        // JSP requires a non-System classloader, this simply wraps the
        // embedded System classloader in a way that makes it suitable
        // for JSP to use
        // new URL( "file:///home/prhodes/.m2/repository/javax/servlet/jsp/javax.servlet.jsp-api/2.3.1/javax.servlet.jsp-api-2.3.1.jar" ) 
        ClassLoader jspClassLoader = new URLClassLoader(new URL[] {}, Thread.currentThread().getContextClassLoader());
        context.setClassLoader(jspClassLoader);

        // Add JSP Servlet (must be named "jsp")
        ServletHolder holderJsp = new ServletHolder("jsp",JspServlet.class);
        holderJsp.setInitOrder(0);
        holderJsp.setInitParameter("logVerbosityLevel","DEBUG");
        holderJsp.setInitParameter("fork","false");
        holderJsp.setInitParameter("xpoweredBy","false");
        holderJsp.setInitParameter("compilerTargetVM","1.7");
        holderJsp.setInitParameter("compilerSourceVM","1.7");
        holderJsp.setInitParameter("keepgenerated","true");
        context.addServlet(holderJsp,"*.jsp");
        //context.addServlet(holderJsp,"*.jspf");
        //context.addServlet(holderJsp,"*.jspx");

        // Add Default Servlet (must be named "default")
        ServletHolder holderDefault = new ServletHolder("default",DefaultServlet.class);
        holderDefault.setInitParameter("resourceBase",baseUri.toASCIIString());
        holderDefault.setInitParameter("dirAllowed","true");
        context.addServlet(holderDefault,"/");         
        
        /** end "for JSP support */
        

        context.setResourceBase(baseUri.toASCIIString());
        
        context.setInitParameter("resteasy.guice.modules", "com.opensoc.dataservices.modules.guice.RestEasyModule");
        context.setInitParameter("resteasy.servlet.mapping.prefix", "/rest");
        
        context.addEventListener(injector.getInstance(GuiceResteasyBootstrapServletContextListener.class));
        context.addFilter(guiceFilter, "/*", EnumSet.allOf(DispatcherType.class));
        
        server.setHandler(context);
        server.start();
        server.join();
    }
}

class DefaultServletModule extends ServletModule {
    
	@Override
    protected void configureServlets() {
        
		ShiroWebModule.bindGuiceFilter(binder());
		
        bind( HttpServletDispatcher.class ).in(Singleton.class);
        serve( "/rest/*").with(HttpServletDispatcher.class);
        
        bind( MessageSenderServlet.class ).in(Singleton.class);
		serve( "/ws/*").with(MessageSenderServlet.class );
		
		bind( LoginServlet.class).in(Singleton.class);
		serve( "/login" ).with( LoginServlet.class );
        
		bind( LogoutServlet.class).in(Singleton.class);
		serve( "/logout" ).with( LogoutServlet.class );
		
    }
}

class DefaultShiroWebModule extends ShiroWebModule {
    DefaultShiroWebModule(ServletContext sc) {
        super(sc);
    }

    protected void configureShiroWeb() {
        bindConstant().annotatedWith(Names.named("shiro.loginUrl")).to( "/login.jsp" );
    	bindRealm().to(JndiLdapRealm.class);
    	bind( LogoutFilter.class);
        
        addFilterChain("/login", ANON);
        addFilterChain("/logout", ANON);
        addFilterChain("/withsocket.jsp", AUTHC );
        addFilterChain("/withsocket2.jsp", ANON );
    }
    
    @Provides JndiLdapRealm providesRealm()
    {
    	JndiLdapContextFactory contextFactory = new JndiLdapContextFactory();
    	// contextFactory.setUrl( "ldap://54.210.98.199:389" );
    	contextFactory.setUrl( "ldap://ec2-54-88-217-194.compute-1.amazonaws.com" );
    	contextFactory.setAuthenticationMechanism( "simple" );
    	JndiLdapRealm realm = new JndiLdapRealm();
    	realm.setContextFactory(contextFactory);
    	return realm;
    }
}