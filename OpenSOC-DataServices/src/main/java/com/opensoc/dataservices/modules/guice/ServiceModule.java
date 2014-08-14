package com.opensoc.dataservices.modules.guice;

import javax.inject.Singleton;

import org.jboss.resteasy.plugins.guice.ext.RequestScopeModule;

import com.google.inject.Provides;
import com.opensoc.dataservices.common.OpenSOCService;
import com.opensoc.services.alerts.ElasticSearch_KafkaAlertsService;
import com.opensoc.services.alerts.Solr_KafkaAlertsService;

public class ServiceModule extends RequestScopeModule {

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
}
