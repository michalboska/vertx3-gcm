package com.github.michalboska.vertx3.gcm;

import com.github.michalboska.vertx3.gcm.internal.GcmServiceImpl;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

@ProxyGen
@VertxGen
public interface GcmService {

    static GcmService create(Vertx vertx) {
        return new GcmServiceImpl(vertx);
    }

    static GcmService createProxy(Vertx vertx, String address) {
//        return new GcmSerVer
        return null;
    }


    void doSomething(SomeDto someDto);
    void save(String str1, Long lng2);
}
