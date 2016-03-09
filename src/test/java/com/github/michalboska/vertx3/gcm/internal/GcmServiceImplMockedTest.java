package com.github.michalboska.vertx3.gcm.internal;

import com.github.michalboska.vertx3.gcm.*;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;


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
        gcmService = new GcmServiceImpl(runWithVertxRule.vertx(), new GcmServiceConfig("apiKey"));
        gcmService.startLocally();
        gcmService.injectHttpClient(httpClient);

        GcmServiceConfig retryConfig = new GcmServiceConfig("apiKey")
                .setBackoffMaxSeconds(20)
                .setBackoffRetries(5);
        gcmServiceWithRetry = new GcmServiceImpl(runWithVertxRule.vertx(), retryConfig);
        gcmServiceWithRetry.startLocally();
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
    public void testServerErrorWithRetryFailDueTimeExceeded(TestContext context) {
        when(httpClient.doRequest(any())).thenAnswer(invocationOnMock -> {
            ObservableFuture<Object> future = RxHelper.observableFuture();
            Future.failedFuture(new GcmHttpException(500, "Server error")).setHandler(future.toHandler());
            Thread.sleep(6000);
            return future;
        });
        gcmServiceWithRetry.sendNotification(mixedNotification, context.asyncAssertFailure(error -> {
            context.assertEquals(GcmHttpException.class, error.getClass());
        }));
    }

    @Test(timeout = 20000)
    public void testServerErrorWithRetryFailDueNumTriesExceeded(TestContext context) {
        when(httpClient.doRequest(any())).thenAnswer(invocationOnMock -> {
            ObservableFuture<Object> future = RxHelper.observableFuture();
            Future.failedFuture(new GcmHttpException(500, "Server error")).setHandler(future.toHandler());
            return future;
        });
        gcmServiceWithRetry.sendNotification(mixedNotification, context.asyncAssertFailure(error -> {
            context.assertEquals(GcmHttpException.class, error.getClass());
        }));
    }

    @Test(timeout = 20000)
    public void testServerErrorWithRetryShouldEventuallySucceedPartially(TestContext context) {
        executeMockTest((notification, counter) -> {
            List<String> registrationIds = notification.getRegistrationIds();
            Map<String, SingleMessageResult> resultMap = new HashMap<>();
            switch (counter) {
                case 1:
                    context.assertEquals(notification, mixedNotification);
                    return mixedResultMap;
                case 2:
                    context.assertEquals(3, registrationIds.size());
                    context.assertTrue(registrationIds.contains("g"));
                    context.assertTrue(registrationIds.contains("h"));
                    context.assertTrue(registrationIds.contains("i"));
                    resultMap.put("g", new SingleMessageResult("gMsg", null, SingleMessageErrorType.INTERNAL_SERVER_ERROR));
                    resultMap.put("h", new SingleMessageResult("hMsg", null, SingleMessageErrorType.UNAVAILABLE));
                    resultMap.put("i", new SingleMessageResult("iMsg", null, null)); //succeed
                    break;
                case 3:
                    context.assertEquals(2, registrationIds.size());
                    context.assertTrue(registrationIds.contains("g"));
                    context.assertTrue(registrationIds.contains("h"));
                    resultMap.put("g", new SingleMessageResult("gMsg", null, SingleMessageErrorType.INTERNAL_SERVER_ERROR));
                    resultMap.put("h", new SingleMessageResult("hMsg", null, null)); //succeed
                    break;
                default: //all further requests
                    context.assertEquals(1, registrationIds.size());
                    context.assertTrue(registrationIds.contains("g"));
                    resultMap.put("g", new SingleMessageResult("gMsg", null, SingleMessageErrorType.INTERNAL_SERVER_ERROR));
                    break;
            }
            return resultMap;
        });
        gcmServiceWithRetry.sendNotification(mixedNotification, context.asyncAssertSuccess(result -> {
            Map<String, SingleMessageResult> deviceResults = result.getDeviceResults();
            context.assertEquals(mixedResultMap.size(), deviceResults.size());
            context.assertEquals(SingleMessageErrorType.INTERNAL_SERVER_ERROR, deviceResults.get("g").getError());

            context.assertTrue(deviceResults
                    .entrySet()
                    .stream()
                    .allMatch(entry -> {
                        if (entry.getValue().getError() != null) {
                            return entry.getKey().equals("g") ? entry.getValue().getError().shouldRetry() : !entry.getValue().getError().shouldRetry();
                        }
                        return true;
                    }), "After running this test case, only device 'g' should have a recoverable error");
        }));
    }

    @Test(timeout = 20000)
    public void testServerErrorWithRetryShouldEventuallySucceedCompletely(TestContext context) {
        executeMockTest((notification, counter) -> {
            List<String> registrationIds = notification.getRegistrationIds();
            Map<String, SingleMessageResult> resultMap = new HashMap<>();
            switch (counter) {
                case 1:
                    context.assertEquals(notification, mixedNotification);
                    return mixedResultMap;
                case 2:
                    context.assertEquals(3, registrationIds.size());
                    context.assertTrue(registrationIds.contains("g"));
                    context.assertTrue(registrationIds.contains("h"));
                    context.assertTrue(registrationIds.contains("i"));
                    resultMap.put("g", new SingleMessageResult("gMsg", null, SingleMessageErrorType.INTERNAL_SERVER_ERROR));
                    resultMap.put("h", new SingleMessageResult("hMsg", null, SingleMessageErrorType.UNAVAILABLE));
                    resultMap.put("i", new SingleMessageResult("iMsg", null, null)); //succeed
                    break;
                case 3:
                    context.assertEquals(2, registrationIds.size());
                    context.assertTrue(registrationIds.contains("g"));
                    context.assertTrue(registrationIds.contains("h"));
                    resultMap.put("g", new SingleMessageResult("gMsg", null, null)); //succeed
                    resultMap.put("h", new SingleMessageResult("hMsg", null, null)); //succeed
                    break;
                default: //all further requests
                    context.fail("Should not make further HTTP requests as all device IDs have, as of now, been notified successfully.");
                    break;
            }
            return resultMap;
        });
        gcmServiceWithRetry.sendNotification(mixedNotification, context.asyncAssertSuccess(result -> {
            Map<String, SingleMessageResult> deviceResults = result.getDeviceResults();
            context.assertEquals(mixedResultMap.size(), deviceResults.size());

            context.assertTrue(deviceResults
                            .entrySet()
                            .stream()
                            .allMatch(entry -> entry.getValue().getSuccess() || !entry.getValue().getError().shouldRetry()),
                    "All device IDs should have finished successfully or with a definitive (non-retriable) error");
        }));
    }

    @FunctionalInterface
    interface MockedResponseGenerator {
        Map<String, SingleMessageResult> createResponse(GcmNotification notification, Integer invocationNumber);
    }

    private void executeMockTest(MockedResponseGenerator generator) {
        AtomicInteger counter = new AtomicInteger(0);
        when(httpClient.doRequest(any())).thenAnswer(invocationOnMock -> {
            GcmNotification notification = (GcmNotification) invocationOnMock.getArguments()[0];
            Map<String, SingleMessageResult> resultMap = generator.createResponse(notification, counter.incrementAndGet());
            GcmResponse response = new GcmResponse(0L, null, 0, 0, 0, resultMap).mergeResponse(new GcmResponse());//merge so that stats are recalculated
            ObservableFuture<GcmResponse> future = RxHelper.<GcmResponse>observableFuture();
            Future.succeededFuture(response).setHandler(future.toHandler());
            return future;
        });
    }

}
