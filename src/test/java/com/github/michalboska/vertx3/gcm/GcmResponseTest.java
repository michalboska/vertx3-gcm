package com.github.michalboska.vertx3.gcm;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GcmResponseTest {

    Map<String, SingleMessageResult> mixedResultMap, successfulResultMap;
    GcmResponse mixedResponse, successfulResponse;


    @Before
    public void init() {
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
    }

    @Test
    public void testInvalidRegistrationIds() {
        Set<String> ids = mixedResponse.getInvalidRegistrationIds();
        assertEquals(2, ids.size());
        assertTrue(ids.contains("e"));
        assertTrue(ids.contains("f"));
        assertTrue(successfulResponse.getInvalidRegistrationIds().isEmpty());
    }

    @Test
    public void testCanonicalIds() {
        Map<String, String> replacements = mixedResponse.getCanonicalIdsMap();
        assertEquals(2, replacements.size());
        assertEquals("cId", replacements.get("c"));
        assertEquals("dId", replacements.get("d"));
        assertTrue(successfulResponse.getCanonicalIdsMap().isEmpty());
    }

    @Test
    public void testRetriableErrors() {
        Set<String> ids = mixedResponse.getDeviceIdsToRetry();
        assertEquals(3, ids.size());
        assertTrue(ids.contains("g"));
        assertTrue(ids.contains("h"));
        assertTrue(ids.contains("i"));
        assertTrue(successfulResponse.getDeviceIdsToRetry().isEmpty());
    }

    @Test
    public void testMerge() {
        int size = mixedResponse.getDeviceResults().size();
        mixedResponse.mergeResponse(successfulResponse);
        assertEquals(size, mixedResponse.getDeviceResults().size());

        assertTrue(mixedResponse.getInvalidRegistrationIds().isEmpty());
        Map<String, String> canonicalIdsMap = mixedResponse.getCanonicalIdsMap();
        assertEquals(1, canonicalIdsMap.size());
        assertEquals("cId", canonicalIdsMap.get("c"));

        Set<String> toRetry = mixedResponse.getDeviceIdsToRetry();
        assertEquals(2, toRetry.size());
        assertTrue(toRetry.contains("h"));
        assertTrue(toRetry.contains("i"));

    }
}
