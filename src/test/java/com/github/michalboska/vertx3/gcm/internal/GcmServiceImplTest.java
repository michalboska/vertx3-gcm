package com.github.michalboska.vertx3.gcm.internal;

import com.github.michalboska.vertx3.gcm.GcmNotification;
import com.github.michalboska.vertx3.gcm.GcmService;
import com.github.michalboska.vertx3.gcm.GcmServiceConfig;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.serviceproxy.ProxyHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.junit.Assert.assertNotNull;

/**
 * @author Michal Boska
 **/
@RunWith(VertxUnitRunner.class)
public class GcmServiceImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GcmServiceImplTest.class);

    private static final String API_KEY = "";

    private GcmServiceImpl gcmService;

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Before
    public void before(TestContext context) {
        Vertx vertx = rule.vertx();
        gcmService = (GcmServiceImpl) GcmService.create(vertx, new GcmServiceConfig(API_KEY));
        assertNotNull(gcmService);
        vertx.deployVerticle(gcmService, context.asyncAssertSuccess());
    }

    @Test(timeout = 10000)
    public void testFoo(TestContext context) {
        Async async = context.async();
        String address = GcmService.class.getCanonicalName();
        MessageConsumer<JsonObject> consumer = ProxyHelper.registerService(GcmService.class, rule.vertx(), gcmService, address);
        GcmService proxy = ProxyHelper.createProxy(GcmService.class, rule.vertx(), address);

        proxy.sendNotification(new GcmNotification(Arrays.asList("adsa", "adsfdsf")), (response -> {
            if (response.failed()) {
                context.fail(response.cause());
            } else {
                LOGGER.info(response.result().toJson());
                async.complete();
            }
        }));
    }

}
