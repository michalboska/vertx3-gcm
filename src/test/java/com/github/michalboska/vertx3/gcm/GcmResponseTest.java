package com.github.michalboska.vertx3.gcm;

import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GcmResponseTest extends AbstractUnitTest {

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
