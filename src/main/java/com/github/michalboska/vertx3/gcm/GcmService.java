package com.github.michalboska.vertx3.gcm;

import com.github.michalboska.vertx3.gcm.internal.GcmServiceImpl;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

@ProxyGen
@VertxGen
public interface GcmService {

    static GcmService create(Vertx vertx, GcmServiceConfig config) {
        return new GcmServiceImpl(config);
    }

    static GcmService createProxy(Vertx vertx, String address) {
        return new GcmServiceVertxEBProxy(vertx, address);
    }

}
