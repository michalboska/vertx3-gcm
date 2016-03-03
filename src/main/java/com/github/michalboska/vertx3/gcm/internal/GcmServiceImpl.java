package com.github.michalboska.vertx3.gcm.internal;

import com.github.michalboska.vertx3.gcm.*;
import io.vertx.core.*;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rx.java.ObservableFuture;
import org.apache.commons.lang3.Validate;


public class GcmServiceImpl extends AbstractVerticle implements GcmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GcmServiceImpl.class);

    private GcmServiceConfig config;
    private GcmServiceVertxProxyHandler handler;

    private boolean started = false;
    private GcmHttpClient httpClient;

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
        ObservableFuture<GcmResponse> future = httpClient.doRequest(notification);
        future.subscribe(response -> {
            handler.handle(Future.succeededFuture(response));
        }, throwable -> {
            handler.handle(Future.failedFuture(throwable));
        });
        return this;
    }

}
