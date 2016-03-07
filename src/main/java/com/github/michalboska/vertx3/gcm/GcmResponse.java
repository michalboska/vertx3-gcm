package com.github.michalboska.vertx3.gcm;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Michal Boska
 */
@DataObject
public class GcmResponse {

    public static final String JSON_MULTICASTID = "multicast_id";
    public static final String JSON_RETRY_AFTER = "retry_after";
    public static final String JSON_SUCCESS_COUNT = "success";
    public static final String JSON_FAILURE_COUNT = "failure";
    public static final String JSON_CANONICAL_IDS = "canonical_ids";
    public static final String JSON_RESULTS = "results";

    private Long multicastId;
    private Integer successCount, failureCount, canonicalIdCount, retryAfterSeconds;
    private Map<String, SingleMessageResult> deviceResults;

    public GcmResponse() {
        deviceResults = new HashMap<>();
    }

    public GcmResponse(GcmResponse copyResponse) {
        this(copyResponse.getMulticastId(),
                copyResponse.getRetryAfterSeconds(),
                copyResponse.getSuccessCount(),
                copyResponse.getFailureCount(),
                copyResponse.getCanonicalIdCount(),
                copyResponse.getDeviceResults());
    }

    public GcmResponse(JsonObject jsonObject) {
        this(jsonObject.getLong(JSON_MULTICASTID),
                jsonObject.getInteger(JSON_RETRY_AFTER),
                jsonObject.getInteger(JSON_SUCCESS_COUNT),
                jsonObject.getInteger(JSON_FAILURE_COUNT),
                jsonObject.getInteger(JSON_CANONICAL_IDS),
                toDeviceIdResultMap(jsonObject.getJsonObject(JSON_RESULTS)));

    }

    public GcmResponse(Long multicastId,
                       Integer retryAfterSeconds,
                       Integer successCount,
                       Integer failureCount,
                       Integer canonicalIdCount,
                       Map<String, SingleMessageResult> deviceResults) {
        this.multicastId = multicastId;
        this.retryAfterSeconds = retryAfterSeconds;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.canonicalIdCount = canonicalIdCount;
        this.deviceResults = deviceResults;
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put(JSON_MULTICASTID, multicastId)
                .put(JSON_RETRY_AFTER, retryAfterSeconds)
                .put(JSON_SUCCESS_COUNT, successCount)
                .put(JSON_FAILURE_COUNT, failureCount)
                .put(JSON_CANONICAL_IDS, canonicalIdCount)
                .put(JSON_RESULTS, fromDeviceIdResultMap(deviceResults));
    }

    /**
     * A convenience method to collect all registration IDs, that were rejected by GCM, because they are invalid.
     * This list does not contain registration IDs that failed due to other reasons than being invalid (for example due to technical difficulties at GCM).
     * <p>
     * IDs returned by this method should presumably be removed from the sender's database.
     *
     * @return A set of registration IDs
     */
    public Set<String> getInvalidRegistrationIds() {
        if (this.failureCount == 0) {
            return Collections.emptySet();
        }
        return this.deviceResults
                .entrySet()
                .stream()
                .filter(entry -> !entry.getValue().getSuccess() && entry.getValue().getError().isErrorDueToWrongRegistrationId())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * A convenience method to collect all registration IDs, that have their newer versions available (for the same device) along with those newer IDs.
     * The sender should update its database to use the newer registration ID for the same device in the future.
     *
     * @return A map of (old registration id -> new registration id) entries
     * @see <a href="https://developers.google.com/cloud-messaging/registration#canonical-ids">GCM documentation - Canonical IDs</a>
     */
    public Map<String, String> getCanonicalIdsMap() {
        if (this.canonicalIdCount == 0) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>(this.canonicalIdCount);
        this.deviceResults
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().getRegistrationId() != null)
                .forEach(entry -> result.put(entry.getKey(), entry.getValue().getRegistrationId()));
        return result;
    }

    /**
     * Get a set of device IDs for which a recoverable error has been encountered and it makes sense to retry the operation
     * after a certain delay
     *
     * @return
     */
    public Set<String> getDeviceIdsToRetry() {
        if (this.failureCount == 0) {
            return Collections.emptySet();
        }
        return this.deviceResults
                .entrySet()
                .stream()
                .filter(entry -> !entry.getValue().getSuccess() && entry.getValue().getError().shouldRetry())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Merge results of other GCM response into this one.
     * Results for device IDs that are contained in this instance will by overwritten by results from {@code otherResponse}
     * and counts will be recalculated. Mutates the current instance.
     *
     * @return this instance for fluent API
     */
    public GcmResponse mergeResponse(GcmResponse otherResponse) {
        otherResponse.getDeviceResults()
                .entrySet()
                .forEach(entry -> deviceResults.put(entry.getKey(), entry.getValue()));
        successCount = 0;
        failureCount = 0;
        canonicalIdCount = 0;
        deviceResults.forEach((String deviceId, SingleMessageResult singleMessageResult) -> {
            if (singleMessageResult.getSuccess()) {
                successCount++;
            } else {
                failureCount++;
            }
            if (singleMessageResult.getRegistrationId() != null) {
                canonicalIdCount++;
            }
        });
        return this;
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

    public Map<String, SingleMessageResult> getDeviceResults() {
        return deviceResults;
    }

    public GcmResponse setDeviceResults(Map<String, SingleMessageResult> deviceResults) {
        this.deviceResults = deviceResults;
        return this;
    }

    private static Map<String, SingleMessageResult> toDeviceIdResultMap(JsonObject jsonObject) {
        HashMap<String, SingleMessageResult> destMap = new HashMap<>(jsonObject.size());
        jsonObject
                .getMap()
                .forEach((String key, Object value) -> {
                    if (!(value instanceof JsonObject)) {
                        throw new IllegalArgumentException("Results field must be a JSON object");
                    }
                    destMap.put(key, new SingleMessageResult((JsonObject) value));
                });
        return destMap;
    }

    private static JsonObject fromDeviceIdResultMap(Map<String, ? super SingleMessageResult> map) {
        JsonObject result = new JsonObject();
        map.entrySet().forEach(entry -> {
            SingleMessageResult value = (SingleMessageResult) entry.getValue();
            result.put(entry.getKey(), value.toJson());
        });
        return result;
    }

    public Integer getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    public GcmResponse setRetryAfterSeconds(Integer retryAfterSeconds) {
        this.retryAfterSeconds = retryAfterSeconds;
        return this;
    }
}


