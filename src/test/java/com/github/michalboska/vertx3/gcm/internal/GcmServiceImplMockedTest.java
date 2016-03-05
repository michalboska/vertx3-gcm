package com.github.michalboska.vertx3.gcm.internal;

import com.github.michalboska.vertx3.gcm.GcmNotification;
import com.github.michalboska.vertx3.gcm.GcmServiceConfig;
import com.github.michalboska.vertx3.gcm.exceptions.InvalidApiKeyException;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rx.java.RxHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;

/**
 * @author Michal Boska
 **/
@RunWith(VertxUnitRunner.class)
public class GcmServiceImplMockedTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GcmServiceImplMockedTest.class);

    @Rule
    public RunTestOnContext runWithVertxRule = new RunTestOnContext();

    @Mock
    private GcmHttpClient httpClient;

    private GcmServiceImpl gcmService;

    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        gcmService = new GcmServiceImpl(new GcmServiceConfig("apiKey"));
        gcmService.startLocally(runWithVertxRule.vertx(), Future.future());
        gcmService.injectHttpClient(httpClient);
    }

    @Test
    public void testHttpClient(TestContext context) {
        ObservableFuture<Object> failedFuture = RxHelper.observableFuture();
        Future.failedFuture(new InvalidApiKeyException("Unauthorized")).setHandler(failedFuture.toHandler());
        doReturn(failedFuture).when(httpClient).doRequest(any());
        gcmService.sendNotification(new GcmNotification(Collections.<String>emptyList()), context.asyncAssertFailure(throwable -> {
            context.assertEquals(InvalidApiKeyException.class, throwable.getClass());
        }));
    }


}
