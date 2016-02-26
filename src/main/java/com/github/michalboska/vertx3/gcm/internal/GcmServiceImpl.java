package com.github.michalboska.vertx3.gcm.internal;

import com.github.michalboska.vertx3.gcm.*;
import io.vertx.core.*;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class GcmServiceImpl extends AbstractVerticle implements GcmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GcmServiceImpl.class);

    private GcmServiceVertxProxyHandler _handler;

    private GcmServiceConfig config;

    public GcmServiceImpl(GcmServiceConfig config) {
        this.config = config;
    }

    @Override
    public void start() throws Exception {
        LOGGER.info("Service started " + config);
    }

    @Override
    public GcmService sendNotification(GcmNotification notification, Handler<AsyncResult<GcmResponse>> handler) {

        return this;
    }
}
