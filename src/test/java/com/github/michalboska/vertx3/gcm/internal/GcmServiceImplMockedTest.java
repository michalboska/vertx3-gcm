package com.github.michalboska.vertx3.gcm.internal;

import com.github.michalboska.vertx3.gcm.AbstractUnitTest;
import com.github.michalboska.vertx3.gcm.GcmResponse;
import com.github.michalboska.vertx3.gcm.GcmServiceConfig;
import com.github.michalboska.vertx3.gcm.exceptions.GcmHttpException;
import com.github.michalboska.vertx3.gcm.exceptions.InvalidApiKeyException;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
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

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;


/**
 * @author Michal Boska
 **/
@RunWith(VertxUnitRunner.class)
public class GcmServiceImplMockedTest extends AbstractUnitTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GcmServiceImplMockedTest.class);

    @Rule
    public RunTestOnContext runWithVertxRule = new RunTestOnContext(new VertxOptions().setBlockedThreadCheckInterval(200000));

    @Mock
    private GcmHttpClient httpClient;

    private GcmServiceImpl gcmService;
    private GcmServiceImpl gcmServiceWithRetry;

    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        gcmService = new GcmServiceImpl(new GcmServiceConfig("apiKey"));
        gcmService.startLocally(runWithVertxRule.vertx(), Future.future());
        gcmService.injectHttpClient(httpClient);

        GcmServiceConfig retryConfig = new GcmServiceConfig("apiKey")
                .setBackoffMaxSeconds(10)
                .setBackoffRetries(5);
        gcmServiceWithRetry = new GcmServiceImpl(retryConfig);
        gcmServiceWithRetry.startLocally(runWithVertxRule.vertx(), Future.future());
        gcmServiceWithRetry.injectHttpClient(httpClient);
    }

    @Test
    public void testInvalidApiKey(TestContext context) {
        ObservableFuture<Object> failedFuture = RxHelper.observableFuture();
        Future.failedFuture(new InvalidApiKeyException("Unauthorized")).setHandler(failedFuture.toHandler());
        doReturn(failedFuture).when(httpClient).doRequest(any());
        gcmService.sendNotification(mixedNotification, context.asyncAssertFailure(throwable -> {
            context.assertEquals(InvalidApiKeyException.class, throwable.getClass());
        }));
    }


    @Test
    public void testUnrecoverableError(TestContext context) {
        ObservableFuture<Object> failedFuture = RxHelper.observableFuture();
        Future.failedFuture(new GcmHttpException(403, (Integer) null, "Forbidden")).setHandler(failedFuture.toHandler());
        doReturn(failedFuture).when(httpClient).doRequest(any());
        gcmService.sendNotification(mixedNotification, context.asyncAssertFailure(throwable -> {
            context.assertEquals(GcmHttpException.class, throwable.getClass());
        }));
    }

    @Test
    public void testSuccess(TestContext context) {
        ObservableFuture<GcmResponse> future = RxHelper.observableFuture();
        Future.succeededFuture(successfulResponse).setHandler(future.toHandler());
        doReturn(future).when(httpClient).doRequest(any());
        gcmService.sendNotification(successfulNotification, context.asyncAssertSuccess(result -> {
            context.assertEquals(successfulResponse, result);
        }));
    }

    @Test
    public void testMixedWithoutRetry(TestContext context) {
        ObservableFuture<GcmResponse> future = RxHelper.observableFuture();
        Future.succeededFuture(mixedResponse).setHandler(future.toHandler());
        doReturn(future).when(httpClient).doRequest(any());
        gcmService.sendNotification(mixedNotification, context.asyncAssertSuccess(result -> {
            context.assertEquals(mixedResponse, result);
        }));
    }

    @Test(timeout = 20000)
    public void testRecoverableServerErrorWithRetryFailAnyway(TestContext context) {
        when(httpClient.doRequest(any())).thenAnswer(invocationOnMock -> {
            ObservableFuture<Object> future = RxHelper.observableFuture();
            Future.failedFuture(new GcmHttpException(500, "Server error")).setHandler(future.toHandler());
            return future;
        });
        gcmServiceWithRetry.sendNotification(mixedNotification, context.asyncAssertFailure(error -> {
            context.assertEquals(GcmHttpException.class, error.getClass());
        }));
    }

}
