package com.github.michalboska.vertx3.gcm.internal;

import com.github.michalboska.vertx3.gcm.GcmService;
import com.github.michalboska.vertx3.gcm.GcmServiceVertxEBProxy;
import com.github.michalboska.vertx3.gcm.GcmServiceVertxProxyHandler;
import com.github.michalboska.vertx3.gcm.SomeDto;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ProxyHelper;

public class GcmServiceImpl implements GcmService {

    private GcmServiceVertxProxyHandler _handler;

    public GcmServiceImpl(Vertx vertx) {
        GcmServiceVertxProxyHandler handler = new GcmServiceVertxProxyHandler(vertx, this);
    }

    @Override
    public void doSomething(SomeDto someDto) {

    }

    @Override
    public void save(String str1, Long lng2) {

    }
}
