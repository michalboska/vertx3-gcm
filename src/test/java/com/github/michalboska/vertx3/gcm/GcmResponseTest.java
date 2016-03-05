package com.github.michalboska.vertx3.gcm;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GcmResponseTest {

    Map<String, SingleMessageResult> resultMap;
    GcmResponse response;


    @Before
    public void init() {
        resultMap = new HashMap<>();
        resultMap.put("a", new SingleMessageResult("aMsg", null, null)); //success
        resultMap.put("b", new SingleMessageResult("bMsg", null, null)); //success
        resultMap.put("c", new SingleMessageResult("cMsg", "cId", null)); //canonical ID
        resultMap.put("d", new SingleMessageResult("dMsg", "dId", null)); //canonical ID
        resultMap.put("e", new SingleMessageResult("eMsg", null, SingleMessageErrorType.INVALID_REGISTRATION)); //regID caused error
        resultMap.put("f", new SingleMessageResult("fMsg", null, SingleMessageErrorType.NOT_REGISTERED)); //regID caused error
        resultMap.put("g", new SingleMessageResult("gMsg", null, SingleMessageErrorType.INTERNAL_SERVER_ERROR)); //retriable error
        resultMap.put("h", new SingleMessageResult("hMsg", null, SingleMessageErrorType.UNAVAILABLE)); //retriable error
        resultMap.put("i", new SingleMessageResult("iMsg", null, SingleMessageErrorType.UNAVAILABLE)); //retriable error
        resultMap.put("j", new SingleMessageResult("jMsg", null, SingleMessageErrorType.MESSAGE_TOO_BIG)); //final error

        response = new GcmResponse(0L, 0, 2, 6, 2, resultMap);
    }

    @Test
    public void testInvalidRegistrationIds() {
        Set<String> ids = response.getInvalidRegistrationIds();
        assertEquals(2, ids.size());
        assertTrue(ids.contains("e"));
        assertTrue(ids.contains("f"));
    }

    @Test
    public void testCanonicalIds() {
        Map<String, String> replacements = response.getRegistrationIdReplacements();
        assertEquals(2, replacements.size());
        assertEquals("cId", replacements.get("c"));
        assertEquals("dId", replacements.get("d"));
    }

    @Test
    public void testRetriableErrors() {
        Set<String> ids = response.getDeviceIdsToRetry();
        assertEquals(3, ids.size());
        assertTrue(ids.contains("g"));
        assertTrue(ids.contains("h"));
        assertTrue(ids.contains("i"));
    }
}
