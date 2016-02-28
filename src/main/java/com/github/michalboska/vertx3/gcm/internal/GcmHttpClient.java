package com.github.michalboska.vertx3.gcm.internal;

import com.github.michalboska.vertx3.gcm.GcmNotification;
import com.github.michalboska.vertx3.gcm.GcmServiceConfig;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.rx.java.ObservableFuture;

public class GcmHttpClient {

    public static final String GCM_SERVER_HOSTNAME = "gcm-http.googleapis.com";
    public static final Integer GCM_SERVER_PORT = 443;
    private static final String GCM_SERVER_URI_PATH = "/gcm/send";

    private Vertx vertx;
    private GcmServiceConfig config;
    private HttpClient httpClient;

    public GcmHttpClient(Vertx vertx, GcmServiceConfig config) {
        this.vertx = vertx;
        this.config = config;
        httpClient = vertx.createHttpClient(new HttpClientOptions()
                .setDefaultHost(GCM_SERVER_HOSTNAME)
                .setDefaultPort(GCM_SERVER_PORT)
                .setSsl(true)
                .setKeepAlive(false)
        );
    }

    public ObservableFuture<JsonObject> doRequest(GcmNotification notification) {
        return null;
    }


}