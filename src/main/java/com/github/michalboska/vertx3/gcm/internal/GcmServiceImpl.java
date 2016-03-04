package com.github.michalboska.vertx3.gcm.internal;

import com.github.michalboska.vertx3.gcm.*;
import com.github.michalboska.vertx3.gcm.exceptions.GcmHttpException;
import io.vertx.core.*;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rx.java.ObservableHandler;
import io.vertx.rx.java.RxHelper;
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
        config.checkState();
        httpClient = new GcmHttpClient(vertx, config);
        handler = new GcmServiceVertxProxyHandler(vertx, this);
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
        config.checkState();
        httpClient = new GcmHttpClient(vertx, config);
        startFuture.complete();
    }

    @Override
    public GcmService sendNotification(GcmNotification notification, Handler<AsyncResult<GcmResponse>> handler) {
        Validate.validState(started, "Service instance has not been started. " +
                "When running this service locally (not as a separately deployed Verticle), use the startLocally method first");
        Validate.validState(stateMap.get(notification) == null, "The supplied GCM notification is already being processed");
        Future<GcmResponse> futureToComplete = Future.<GcmResponse>future().setHandler(handler);
        stateMap.put(notification, new NotificationState(futureToComplete));
        ObservableFuture<GcmResponse> requestFuture = httpClient.doRequest(notification);
        requestFuture.subscribe(response -> {
            handleSuccess(notification, response);
        }, throwable -> {
            handleError(notification, throwable);
        });
        return this;
    }

    private void handleSuccess(GcmNotification notification, GcmResponse response) {
        NotificationState state = stateMap.get(notification);
        Future<GcmResponse> responseFuture = state.completionFuture;
        state.updateLastTry(response);

        //If all device IDs have succeeded, or the notification has already taken too long, or there are errors, but none of them is retriable,
        //just give up and return what we got
        Set<String> deviceIdsToRetry = response.getDeviceIdsToRetry();
        if (response.getFailureCount() == 0 || deviceIdsToRetry.isEmpty() || state.hasExpired()) {
            stateMap.remove(notification);
            responseFuture.complete(response);
        } else {
            GcmNotification notificationToRetry = notification.copyWithDeviceIdList(deviceIdsToRetry);
            retryNotification(notification, notificationToRetry);
        }
    }

    private void handleError(GcmNotification notification, Throwable error) {
        NotificationState state = stateMap.get(notification);
        state.updateLastTry(null);
        if (error instanceof GcmHttpException) {
            GcmHttpException httpException = (GcmHttpException) error;
            if (httpException.shouldRetry() && !state.hasExpired()) {
                retryNotification(notification, notification);
            } else {
                stateMap.remove(notification);
                state.completionFuture.fail(error);
            }
        } else {
            stateMap.remove(notification);
            state.completionFuture.fail(error);
        }
    }

    private void retryNotification(GcmNotification originalNotification, GcmNotification retryWithNotification) {
        NotificationState state = stateMap.get(originalNotification);
        ObservableHandler<Long> timerHandler = RxHelper.observableHandler(false);
        vertx.setTimer(state.secondsIncrement * 1000, timerHandler.toHandler());
        timerHandler
                .concatMap((timerId) -> httpClient.doRequest(retryWithNotification))
                .subscribe(response -> {
                    handleSuccess(originalNotification, response);
                }, error -> {
                    handleError(originalNotification, error);
                });
    }

    private class NotificationState {
        int tries = 0, totalSecondsPassed = 0, secondsIncrement = 2;
        LocalDateTime lastSent;
        GcmResponse currentResponse;
        private Future<GcmResponse> completionFuture;

        NotificationState(Future<GcmResponse> completionFuture) {
            lastSent = LocalDateTime.now();
            this.completionFuture = completionFuture;
        }

        void updateLastTry(GcmResponse newResponse) {
            LocalDateTime now = LocalDateTime.now();
            Duration timeElapsed = Duration.between(lastSent, now);
            totalSecondsPassed += timeElapsed.getSeconds();
            lastSent = now;
            tries++;
            secondsIncrement *= 2;
            if (currentResponse == null) {
                currentResponse = newResponse;
            } else if (newResponse != null) {
                currentResponse.mergeResponse(newResponse);
            }
        }

        boolean hasExpired() {
            return totalSecondsPassed + secondsIncrement >= GcmServiceImpl.this.config.getBackoffMaxSeconds()
                    || tries >= GcmServiceImpl.this.config.getBackoffRetries();
        }
    }

}
