package com.github.michalboska.vertx3.gcm.internal;

import com.github.michalboska.vertx3.gcm.GcmNotification;
import com.github.michalboska.vertx3.gcm.GcmServiceConfig;
import com.github.michalboska.vertx3.gcm.exceptions.GcmException;
import com.github.michalboska.vertx3.gcm.exceptions.GcmHttpException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rx.java.ObservableHandler;
import io.vertx.rx.java.RxHelper;

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
        ObservableFuture<JsonObject> resultFuture = RxHelper.<JsonObject>observableFuture();
        Handler<AsyncResult<JsonObject>> resultHandler = resultFuture.toHandler();

        ObservableHandler<HttpClientResponse> httpResponseObservable = RxHelper.observableHandler(false);
        HttpClientRequest request = httpClient.post(GCM_SERVER_URI_PATH, httpResponseObservable.toHandler());
        request.putHeader("Content-Type", "application/json");
        request.putHeader("Authorization", String.format("key=%s", config.getApiKey()));
        request.end(notification.toJson().encode());

        httpResponseObservable.subscribe((httpClientResponse) -> {
            httpClientResponse.bodyHandler((bodyBuffer -> {
                if (httpClientResponse.statusCode() == 200) {
                    resultHandler.handle(Future.succeededFuture(bodyBuffer.toJsonObject()));
                } else {
                    resultHandler.handle(Future.failedFuture(new GcmHttpException(httpClientResponse.statusCode(), httpClientResponse.statusMessage(), bodyBuffer.toString())));
                }
            }));
        }, (throwable) -> {
            resultHandler.handle(Future.failedFuture(new GcmException("Could not send GCM request", throwable)));
        });
        return resultFuture;
    }

}