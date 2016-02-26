package com.github.michalboska.vertx3.gcm.internal;

import com.github.michalboska.vertx3.gcm.*;
import io.vertx.core.*;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.serviceproxy.ProxyHelper;

public class GcmServiceImpl extends AbstractVerticle implements GcmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GcmServiceImpl.class);

    private MessageConsumer<JsonObject> messageConsumer;

    private GcmServiceConfig config;

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
//        new GcmServiceVertxProxyHandler(vertx, this).registerHandler()

        LOGGER.info(String.format("GCM Client service started and listening on EventBus address: %s", config.getAddress()));
        startFuture.complete();
    }

    @Override
    public GcmService sendNotification(GcmNotification notification, Handler<AsyncResult<GcmResponse>> handler) {
        vertx.setTimer(2500, (id) -> handler.handle(new AsyncResult<GcmResponse>() {
            @Override
            public GcmResponse result() {
                return new GcmResponse();
            }

            @Override
            public Throwable cause() {
                return null;
            }

            @Override
            public boolean succeeded() {
                return true;
            }

            @Override
            public boolean failed() {
                return false;
            }
        }));
        return this;
    }
}
