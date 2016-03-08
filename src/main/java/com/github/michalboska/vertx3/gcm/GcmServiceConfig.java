package com.github.michalboska.vertx3.gcm;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.Validate;

/**
 * @author Michal Boska
 */
@DataObject
public class GcmServiceConfig {

    private static String JSON_REGLIMIT = "registrationIdsLimit";
    private static String JSON_MAXSEXONDS = "maxSecondsToLeave";
    private static String JSON_BACKOFF_RETRIES = "backoffRetries";
    private static String JSON_BACKOFF_MAX_SECONDS = "backoffMaxSeconds";
    private static String JSON_API_KEY = "apiKey";
    private static String JSON_ADDRESS = "address";
    private static String JSON_LOCAL_ONLY = "localOnly";

    ///MANDATORY FIELDS
    String apiKey;

    ///OPTIONAL FIELDS
    String address = GcmService.class.getName();
    Integer registrationIdsLimit = 1000;
    Integer maxSecondsToLeave = 2419200;
    Integer backoffRetries = 0;
    Integer backoffMaxSeconds = 600;
    Boolean localOnly = false;

    public GcmServiceConfig() {
    }

    public GcmServiceConfig(String apiKey) {
        this.apiKey = apiKey;
    }

    public GcmServiceConfig(String address, String apiKey) {
        this.address = address;
        this.apiKey = apiKey;
    }

    public GcmServiceConfig(Integer registrationIdsLimit, Integer maxSecondsToLeave, Integer backoffRetries, Integer backoffMaxSeconds, String apiKey, String address, Boolean localOnly) {
        this.registrationIdsLimit = registrationIdsLimit;
        this.maxSecondsToLeave = maxSecondsToLeave;
        this.backoffRetries = backoffRetries;
        this.backoffMaxSeconds = backoffMaxSeconds;
        this.apiKey = apiKey;
        this.address = address;
        this.localOnly = localOnly;
    }

    public GcmServiceConfig(GcmServiceConfig copyConfig) {
        this(copyConfig.getRegistrationIdsLimit(),
                copyConfig.getMaxSecondsToLeave(),
                copyConfig.getBackoffRetries(),
                copyConfig.getBackoffMaxSeconds(),
                copyConfig.getApiKey(),
                copyConfig.getAddress(),
                copyConfig.getLocalOnly());
    }

    public GcmServiceConfig(JsonObject jsonObject) {
        this(jsonObject.getInteger(JSON_REGLIMIT),
                jsonObject.getInteger(JSON_MAXSEXONDS),
                jsonObject.getInteger(JSON_BACKOFF_RETRIES),
                jsonObject.getInteger(JSON_BACKOFF_MAX_SECONDS),
                jsonObject.getString(JSON_API_KEY),
                jsonObject.getString(JSON_ADDRESS),
                jsonObject.getBoolean(JSON_LOCAL_ONLY));
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put(JSON_REGLIMIT, registrationIdsLimit)
                .put(JSON_MAXSEXONDS, maxSecondsToLeave)
                .put(JSON_BACKOFF_RETRIES, backoffRetries)
                .put(JSON_BACKOFF_MAX_SECONDS, backoffMaxSeconds)
                .put(JSON_API_KEY, apiKey)
                .put(JSON_ADDRESS, address)
                .put(JSON_LOCAL_ONLY, localOnly);
    }

    public GcmServiceConfig checkState() throws IllegalStateException {
        Validate.validState(apiKey != null, "Api key must be set");
        Validate.validState(address != null, "Eventbus address to listen on must be set");
        return this;
    }

    /**
     * Maximum number of registration IDs sent in a single notification. If a notification contains more IDs than this, an exception is thrown.
     * @return
     */
    public Integer getRegistrationIdsLimit() {
        return registrationIdsLimit;
    }

    public GcmServiceConfig setRegistrationIdsLimit(Integer registrationIdsLimit) {
        this.registrationIdsLimit = registrationIdsLimit;
        return this;
    }

    /**
     * Maximum number of seconds that can be sent as notification's {@link GcmNotification#getTtlSeconds() ttlSeconds} {@code ttlSeconds} parameter
     * @return
     */
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

    public String getApiKey() {
        return apiKey;
    }

    public GcmServiceConfig setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public GcmServiceConfig setAddress(String address) {
        this.address = address;
        return this;
    }

    public Boolean getLocalOnly() {
        return localOnly;
    }

    public GcmServiceConfig setLocalOnly(Boolean localOnly) {
        this.localOnly = localOnly;
        return this;
    }

    public Integer getBackoffMaxSeconds() {
        return backoffMaxSeconds;
    }

    public GcmServiceConfig setBackoffMaxSeconds(Integer backoffMaxSeconds) {
        this.backoffMaxSeconds = backoffMaxSeconds;
        return this;
    }
}
