package com.github.michalboska.vertx3.gcm;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michal Boska
 */
@DataObject
public class GcmResponse {

    private static String JSON_MULTICASTID = "multicast_id";
    private static String JSON_SUCCESS_COUNT = "success";
    private static String JSON_FAILURE_COUNT = "failure";
    private static String JSON_CANONICAL_IDS = "canonical_ids";
    private static String JSON_RESULTS = "results";

    private Long multicastId;
    private Integer successCount, failureCount, canonicalIdCount;
    private Map<String, DeviceIdResult> deviceResults;

    public GcmResponse() {
        deviceResults = Collections.emptyMap();
    }

    public GcmResponse(GcmResponse copyResponse) {
        this(copyResponse.getMulticastId(),
                copyResponse.getSuccessCount(),
                copyResponse.getFailureCount(),
                copyResponse.getCanonicalIdCount(),
                copyResponse.getDeviceResults());
    }

    public GcmResponse(JsonObject jsonObject) {
        this(jsonObject.getLong(JSON_MULTICASTID),
                jsonObject.getInteger(JSON_SUCCESS_COUNT),
                jsonObject.getInteger(JSON_FAILURE_COUNT),
                jsonObject.getInteger(JSON_CANONICAL_IDS),
                toDeviceIdResultMap(jsonObject.getJsonObject(JSON_RESULTS)));

    }

    public GcmResponse(Long multicastId, Integer successCount, Integer failureCount, Integer canonicalIdCount, Map<String, DeviceIdResult> deviceResults) {
        this.multicastId = multicastId;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.canonicalIdCount = canonicalIdCount;
        this.deviceResults = deviceResults;
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put(JSON_MULTICASTID, multicastId)
                .put(JSON_SUCCESS_COUNT, successCount)
                .put(JSON_FAILURE_COUNT, failureCount)
                .put(JSON_CANONICAL_IDS, canonicalIdCount)
                .put(JSON_RESULTS, fromDeviceIdResultMap(deviceResults));
    }

    public Long getMulticastId() {
        return multicastId;
    }

    public GcmResponse setMulticastId(Long multicastId) {
        this.multicastId = multicastId;
        return this;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public GcmResponse setSuccessCount(Integer successCount) {
        this.successCount = successCount;
        return this;
    }

    public Integer getFailureCount() {
        return failureCount;
    }

    public GcmResponse setFailureCount(Integer failureCount) {
        this.failureCount = failureCount;
        return this;
    }

    public Integer getCanonicalIdCount() {
        return canonicalIdCount;
    }

    public GcmResponse setCanonicalIdCount(Integer canonicalIdCount) {
        this.canonicalIdCount = canonicalIdCount;
        return this;
    }

    public Map<String, DeviceIdResult> getDeviceResults() {
        return deviceResults;
    }

    public GcmResponse setDeviceResults(Map<String, DeviceIdResult> deviceResults) {
        this.deviceResults = deviceResults;
        return this;
    }

    private static Map<String, DeviceIdResult> toDeviceIdResultMap(JsonObject jsonObject) {
        HashMap<String, DeviceIdResult> destMap = new HashMap<>(jsonObject.size());
        jsonObject
                .getMap()
                .forEach((String key, Object value) -> {
                    if (!(value instanceof JsonObject)) {
                        throw new IllegalArgumentException("Results field must be a JSON object");
                    }
                    destMap.put(key, new DeviceIdResult((JsonObject) value));
                });
        return destMap;
    }

    private static JsonObject fromDeviceIdResultMap(Map<String, ? super DeviceIdResult> map) {
        return new JsonObject((Map<String, Object>) map);
    }
}


