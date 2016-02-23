package com.github.michalboska.vertx3.gcm;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * @author Michal Boska
 */
@DataObject
public class GcmServiceConfig {

    private static String JSON_REGLIMIT = "registrationIdsLimit";
    private static String JSON_MAXSEXONDS = "maxSecondsToLeave";
    private static String JSON_BACKOFF_RETRIES = "backoffRetries";
    private static String JSON_API_KEY = "apiKey";

    ///MANDATORY FIELDS
    String apiKey;

    ///OPTIONAL FIELDS
    Integer registrationIdsLimit = 1000;
    Integer maxSecondsToLeave = 2419200;
    Integer backoffRetries = 5;

    public GcmServiceConfig() {
    }

    public GcmServiceConfig(String apiKey) {
        this.apiKey = apiKey;
    }

    public GcmServiceConfig(Integer registrationIdsLimit, Integer maxSecondsToLeave, Integer backoffRetries, String apiKey) {
        this.registrationIdsLimit = registrationIdsLimit;
        this.maxSecondsToLeave = maxSecondsToLeave;
        this.backoffRetries = backoffRetries;
        this.apiKey = apiKey;
    }

    public GcmServiceConfig(GcmServiceConfig copyConfig) {
        this(copyConfig.getRegistrationIdsLimit(), copyConfig.getMaxSecondsToLeave(), copyConfig.getBackoffRetries(), copyConfig.apiKey);
    }

    public GcmServiceConfig(JsonObject jsonObject) {
        this(jsonObject.getInteger(JSON_REGLIMIT),
                jsonObject.getInteger(JSON_MAXSEXONDS),
                jsonObject.getInteger(JSON_BACKOFF_RETRIES),
                jsonObject.getString(JSON_API_KEY));
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put(JSON_REGLIMIT, registrationIdsLimit)
                .put(JSON_MAXSEXONDS, maxSecondsToLeave)
                .put(JSON_BACKOFF_RETRIES, backoffRetries)
                .put(JSON_API_KEY, apiKey);
    }

    public GcmServiceConfig checkState() throws IllegalStateException {
        if (apiKey == null) {
            throw new IllegalStateException("Api key must be set");
        }
        return this;
    }

    public Integer getRegistrationIdsLimit() {
        return registrationIdsLimit;
    }

    public GcmServiceConfig setRegistrationIdsLimit(Integer registrationIdsLimit) {
        this.registrationIdsLimit = registrationIdsLimit;
        return this;
    }

    public Integer getMaxSecondsToLeave() {
        return maxSecondsToLeave;
    }

    public GcmServiceConfig setMaxSecondsToLeave(Integer maxSecondsToLeave) {
        this.maxSecondsToLeave = maxSecondsToLeave;
        return this;
    }

    public Integer getBackoffRetries() {
        return backoffRetries;
    }

    public GcmServiceConfig setBackoffRetries(Integer backoffRetries) {
        this.backoffRetries = backoffRetries;
        return this;
    }

}
