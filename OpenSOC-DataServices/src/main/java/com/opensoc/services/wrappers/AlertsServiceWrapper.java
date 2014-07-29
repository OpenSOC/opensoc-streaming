package com.opensoc.services.wrappers;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.opensoc.dataservices.common.OpenSOCService;

@Path("/")
@Singleton
public class AlertsServiceWrapper {

    @Inject OpenSOCService AlertsServiceImpl;

    @GET
    @Path("/identify")
    @Produces(MediaType.APPLICATION_JSON)
    public Data getJsonMessage() {
        return new Data(AlertsServiceImpl.identify());
    }


    @GET
    @Path("/async-identify")
    @Produces(MediaType.TEXT_PLAIN)
    public void getAsyncData(@Suspended final AsyncResponse response,
                             @QueryParam("d") @DefaultValue("1") final int delaySec) throws IOException {

        response.setTimeout(6, TimeUnit.SECONDS);

        new Thread(() -> {
            try {
                Thread.sleep(delaySec * 1000);

                if (!response.isSuspended()) {
                    System.out.println("Async response is not suspended");
                    return;
                }

                if (!response.resume(Response.ok(AlertsServiceImpl.identify()).build())) {
                    System.out.println("Async response not resumed");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public static class Data {

        private final String text;

        public Data(String data) {
            this.text = data;
        }

        public String getData() {
            return text;
        }
    }
}
