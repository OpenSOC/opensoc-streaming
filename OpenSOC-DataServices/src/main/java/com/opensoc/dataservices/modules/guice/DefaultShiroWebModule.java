package com.opensoc.dataservices.modules.guice;

import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.shiro.guice.web.ShiroWebModule;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.apache.shiro.realm.ldap.JndiLdapRealm;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provides;
import com.google.inject.name.Names;

public class DefaultShiroWebModule extends ShiroWebModule {
    
	private static final Logger logger = LoggerFactory.getLogger( DefaultShiroWebModule.class );
	
	private Properties configProps;
	
	public DefaultShiroWebModule(final ServletContext sc) {
        super(sc);
    }

    public DefaultShiroWebModule(final Properties configProps, final ServletContext sc) {
        super(sc);
        this.configProps = configProps;
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
    
    @Provides 
    @javax.inject.Singleton 
    JndiLdapRealm providesRealm()
    {
    	// pull our ldap url, etc., from config
    	String ldapUrl = configProps.getProperty("ldapUrl");
    	logger.info( "got ldapurl from config: " + ldapUrl );
    	
    	JndiLdapContextFactory contextFactory = new JndiLdapContextFactory();
    	contextFactory.setUrl( ldapUrl );
    	contextFactory.setAuthenticationMechanism( "simple" );
    	JndiLdapRealm realm = new JndiLdapRealm();
    	realm.setContextFactory(contextFactory);
    	
    	return realm;
    }
}