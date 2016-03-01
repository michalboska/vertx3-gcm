package com.github.michalboska.vertx3.gcm.internal;

import com.github.michalboska.vertx3.gcm.GcmNotification;
import com.github.michalboska.vertx3.gcm.GcmResponse;
import com.github.michalboska.vertx3.gcm.GcmServiceConfig;
import com.github.michalboska.vertx3.gcm.SingleMessageResult;
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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rx.java.ObservableHandler;
import io.vertx.rx.java.RxHelper;
import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GcmHttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GcmHttpClient.class);

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

    public ObservableFuture<GcmResponse> doRequest(GcmNotification notification) {
        ObservableFuture<GcmResponse> resultFuture = RxHelper.observableFuture();
        Handler<AsyncResult<GcmResponse>> resultHandler = resultFuture.toHandler();

        ObservableHandler<HttpClientResponse> httpResponseObservable = RxHelper.observableHandler(false);
        HttpClientRequest request = httpClient.post(GCM_SERVER_URI_PATH, httpResponseObservable.toHandler());
        request.putHeader("Content-Type", "application/json");
        request.putHeader("Authorization", String.format("key=%s", config.getApiKey()));
        request.end(notification.toJson().encode());

        List<String> registrationIds = notification.getRegistrationIds();
        httpResponseObservable.subscribe((httpClientResponse) -> {
            httpClientResponse.bodyHandler((bodyBuffer -> {
                if (httpClientResponse.statusCode() == 200) {
                    resultHandler.handle(Future.succeededFuture(responseJsonToDto(bodyBuffer.toJsonObject(), registrationIds)));
                } else {
                    resultHandler.handle(Future.failedFuture(new GcmHttpException(httpClientResponse.statusCode(), httpClientResponse.statusMessage(), bodyBuffer.toString())));
                }
            }));
        }, (throwable) -> {
            resultHandler.handle(Future.failedFuture(new GcmException("Could not send GCM request", throwable)));
        });
        return resultFuture;
    }

    private GcmResponse responseJsonToDto(JsonObject jsonObject, List<String> requestRegistrationIds) {
        JsonArray results = jsonObject.getJsonArray("results");
        Map<String, SingleMessageResult> singleMessageResultMap;
        if (results.size() == 0) {
            singleMessageResultMap = Collections.emptyMap();
        } else {
            Validate.validState(requestRegistrationIds.size() == results.size());
            singleMessageResultMap = new HashMap<>(results.size());
            for (int i = 0; i < results.size(); i++) {
                JsonObject singleResult = results.getJsonObject(i);
                String registrationId = requestRegistrationIds.get(i);
                singleMessageResultMap.put(registrationId, new SingleMessageResult(singleResult));
            }
        }
        return new GcmResponse(jsonObject.getLong(GcmResponse.JSON_MULTICASTID),
                jsonObject.getInteger(GcmResponse.JSON_SUCCESS_COUNT),
                jsonObject.getInteger(GcmResponse.JSON_FAILURE_COUNT),
                jsonObject.getInteger(GcmResponse.JSON_CANONICAL_IDS),
                singleMessageResultMap);
    }

}