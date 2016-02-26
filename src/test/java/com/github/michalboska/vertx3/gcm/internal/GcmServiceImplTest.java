package com.github.michalboska.vertx3.gcm.internal;

import com.github.michalboska.vertx3.gcm.GcmService;
import com.github.michalboska.vertx3.gcm.GcmServiceConfig;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rx.java.ObservableHandler;
import io.vertx.rx.java.RxHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * @author Michal Boska
 **/
@RunWith(VertxUnitRunner.class)
public class GcmServiceImplTest {

    private GcmServiceImpl gcmService;

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Before
    public void before(TestContext context) {
        Vertx vertx = rule.vertx();
        gcmService = (GcmServiceImpl) GcmService.create(vertx, new GcmServiceConfig(""));
        assertNotNull(gcmService);
        vertx.deployVerticle(gcmService, context.asyncAssertSuccess());
    }

    @Test(timeout = 5000)
    public void testFoo(TestContext context) {

    }

}
