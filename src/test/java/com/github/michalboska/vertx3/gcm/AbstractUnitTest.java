package com.github.michalboska.vertx3.gcm;

import org.junit.Before;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractUnitTest {

    protected Map<String, SingleMessageResult> mixedResultMap, successfulResultMap;
    protected GcmResponse mixedResponse, successfulResponse;
    protected GcmNotification mixedNotification, successfulNotification;

    @Before
    public void initTestData() {
        mixedResultMap = new HashMap<>();
        mixedResultMap.put("a", new SingleMessageResult("aMsg", null, null)); //success
        mixedResultMap.put("b", new SingleMessageResult("bMsg", null, null)); //success
        mixedResultMap.put("c", new SingleMessageResult("cMsg", "cId", null)); //canonical ID
        mixedResultMap.put("d", new SingleMessageResult("dMsg", "dId", null)); //canonical ID
        mixedResultMap.put("e", new SingleMessageResult("eMsg", null, SingleMessageErrorType.INVALID_REGISTRATION)); //regID caused error
        mixedResultMap.put("f", new SingleMessageResult("fMsg", null, SingleMessageErrorType.NOT_REGISTERED)); //regID caused error
        mixedResultMap.put("g", new SingleMessageResult("gMsg", null, SingleMessageErrorType.INTERNAL_SERVER_ERROR)); //retriable error
        mixedResultMap.put("h", new SingleMessageResult("hMsg", null, SingleMessageErrorType.UNAVAILABLE)); //retriable error
        mixedResultMap.put("i", new SingleMessageResult("iMsg", null, SingleMessageErrorType.UNAVAILABLE)); //retriable error
        mixedResultMap.put("j", new SingleMessageResult("jMsg", null, SingleMessageErrorType.MESSAGE_TOO_BIG)); //final error
        mixedResponse = new GcmResponse(0L, 0, 2, 6, 2, mixedResultMap);

        successfulResultMap = new HashMap<>();
        successfulResultMap.put("d", new SingleMessageResult("dMsg", null, null));
        successfulResultMap.put("e", new SingleMessageResult("eMsg", null, null));
        successfulResultMap.put("f", new SingleMessageResult("fMsg", null, null));
        successfulResultMap.put("g", new SingleMessageResult("gMsg", null, null));
        successfulResponse = new GcmResponse(0L, 0, 4, 0, 0, successfulResultMap);

        mixedNotification = new GcmNotification(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j"));
        successfulNotification = new GcmNotification(Arrays.asList("d", "e", "f", "g"));
    }

}
