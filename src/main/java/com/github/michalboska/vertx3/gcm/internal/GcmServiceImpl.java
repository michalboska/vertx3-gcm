package com.github.michalboska.vertx3.gcm.internal;

import com.github.michalboska.vertx3.gcm.GcmService;
import com.github.michalboska.vertx3.gcm.SomeDto;
import io.vertx.core.Vertx;

public class GcmServiceImpl implements GcmService {

    private Vertx vertx;

    public GcmServiceImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void doSomething(SomeDto someDto) {

    }

    @Override
    public void save(String str1, Long lng2) {

    }
}
