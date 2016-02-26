package com.github.michalboska.vertx3.gcm.internal;

import com.github.michalboska.vertx3.gcm.GcmService;
import com.github.michalboska.vertx3.gcm.GcmServiceConfig;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.serviceproxy.ProxyHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

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
//        vertx.deployVerticle(gcmService, context.asyncAssertSuccess());
    }

    @Test(timeout = 5000)
    public void testFoo(TestContext context) {
        String address = GcmService.class.getCanonicalName();
        MessageConsumer<JsonObject> consumer = ProxyHelper.registerService(GcmService.class, rule.vertx(), gcmService, address);
        GcmService proxy = ProxyHelper.createProxy(GcmService.class, rule.vertx(), address);

        proxy.sendNotification(null, context.asyncAssertSuccess());
    }

}
