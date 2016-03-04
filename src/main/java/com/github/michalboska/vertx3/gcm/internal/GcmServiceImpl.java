package com.github.michalboska.vertx3.gcm.internal;

import com.github.michalboska.vertx3.gcm.*;
import com.github.michalboska.vertx3.gcm.exceptions.GcmHttpException;
import io.vertx.core.*;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rx.java.ObservableFuture;
import org.apache.commons.lang3.Validate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class GcmServiceImpl extends AbstractVerticle implements GcmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GcmServiceImpl.class);

    private GcmServiceConfig config;
    private GcmServiceVertxProxyHandler handler;
    private GcmHttpClient httpClient;

    private boolean started = false;
    private Map<GcmNotification, NotificationState> stateMap = new HashMap<>();

    public GcmServiceImpl(GcmServiceConfig config) {
        config.checkState();
        this.config = config;
        LOGGER.info("Instance created");
    }

    public GcmServiceImpl() {
        throw new UnsupportedOperationException("Directly deploying this service without supplying config options is not supported. " +
                "Please use the GcmService interface to create the service instance.");
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        doStart(startFuture);
    }

    /**
     * Start the service in local mode (as a part of another verticle).
     * Use this to initialize the service when not deploying as a separate verticle
     *
     * @param vertx
     * @param startFuture
     * @throws Exception
     */
    public void startLocally(Vertx vertx, Future<Void> startFuture) throws Exception {
        this.vertx = vertx;
        this.context = vertx.getOrCreateContext();
        doStart(startFuture);
    }

    private void doStart(Future<Void> startFuture) {
        config.checkState();
        handler = new GcmServiceVertxProxyHandler(vertx, this);
        httpClient = new GcmHttpClient(vertx, config);
        MessageConsumer<JsonObject> messageConsumer = config.getLocalOnly() ? handler.registerLocalHandler(config.getAddress()) : handler.registerHandler(config.getAddress());
        messageConsumer.completionHandler(ar -> {
            if (ar.succeeded()) {
                started = true;
                LOGGER.info(String.format("GCM Client service started and listening on EventBus address: %s", config.getAddress()));
                startFuture.complete();
            } else {
                startFuture.fail(ar.cause());
            }
        });
    }

    @Override
    public GcmService sendNotification(GcmNotification notification, Handler<AsyncResult<GcmResponse>> handler) {
        Validate.validState(started, "Service instance has not been started. " +
                "When running this service locally (not as a separately deployed Verticle), use the startLocally method first");
        Validate.validState(stateMap.get(notification) == null, "The supplied GCM notification is already being processed");
        stateMap.put(notification, new NotificationState());
        doSendNotification(notification, handler);
        return this;
    }

    private void doSendNotification(GcmNotification notification, Handler<AsyncResult<GcmResponse>> handler) {
        ObservableFuture<GcmResponse> future = httpClient.doRequest(notification);
        future.subscribe(response -> {
            handleSuccess(notification, response, handler);
        }, throwable -> {
            handleError(notification, throwable, handler);
        });
    }

    private void handleSuccess(GcmNotification notification, GcmResponse response, Handler<AsyncResult<GcmResponse>> handler) {
        NotificationState state = stateMap.get(notification);
        Set<String> deviceIdsToRetry = response.getDeviceIdsToRetry();
        state.updateLastTry(response);
        //If all device IDs have succeeded, or the notification has already taken too long, or there are errors, but none of them is retriable,
        //just give up and return what we got
        if (response.getFailureCount() == 0 || deviceIdsToRetry.isEmpty() || state.hasExpired()) {
            stateMap.remove(notification);
            handler.handle(Future.succeededFuture(response));
        } else {
            GcmNotification retryNotification = notification.copyWithDeviceIdList(deviceIdsToRetry);
            doSendNotification(retryNotification, );

        }
    }

    private void handleError(GcmNotification notification, Throwable error, Handler<AsyncResult<GcmResponse>> handler) {
        if (error instanceof GcmHttpException) {
            GcmHttpException httpException = (GcmHttpException) error;
            NotificationState state = stateMap.get(notification);
            if (httpException.shouldRetry() && !state.hasExpired()) {
                state.updateLastTry(null);
                doSendNotification(notification, handler);
            } else {
                stateMap.remove(notification);
                handler.handle(Future.failedFuture(error));
            }
        } else {
            stateMap.remove(notification);
            handler.handle(Future.failedFuture(error));
        }
    }

    private void retryNotification() {

    }

    private class NotificationState {
        int tries = 0, secondsPassed = 0;
        LocalDateTime lastSent;
        GcmResponse currentResponse;

        NotificationState() {
            lastSent = LocalDateTime.now();
        }

        void updateLastTry(GcmResponse newResponse) {
            LocalDateTime now = LocalDateTime.now();
            Duration timeElapsed = Duration.between(lastSent, now);
            secondsPassed += timeElapsed.getSeconds();
            lastSent = now;
            tries++;
            if (currentResponse == null) {
                currentResponse = newResponse;
            } else if (newResponse != null) {
                currentResponse.mergeResponse(newResponse);
            }
        }

        boolean hasExpired() {
            return secondsPassed >= GcmServiceImpl.this.config.getBackoffMaxSeconds()
                    || tries >= GcmServiceImpl.this.config.getBackoffRetries();
        }
    }

}
