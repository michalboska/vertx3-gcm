package com.github.michalboska.vertx3.gcm;

import java.util.HashSet;
import java.util.Set;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * @author Michal Boska
 */
@DataObject
public class GcmNotification {

    private static String JSON_REG_IDS = "registrationIds";
    private static String JSON_COLLAPSE_KEY = "collapseKey";
    private static String JSON_DATA = "data";
    private static String JSON_DELAY_WHILE_IDLE = "delayWhileIdle";
    private static String JSON_TTL_SECONDS = "ttlSeconds";
    private static String JSON_RESTRICT_PACKAGE_NAME = "restrictPackageName";
    private static String JSON_DRY_RUN = "dryRun";

    ///MANDATORY FIELDS
    private Set<String> registrationIds;

    ///OPTIONAL FIELDS
    private String collapseKey;
    private JsonObject data;
    private Boolean delayWhileIdle = false;
    private Long ttlSeconds;
    private String restrictPackageName;
    private Boolean dryRun = false;

    public GcmNotification() {
    }

    public GcmNotification(Set<String> registrationIds) {
        this.registrationIds = registrationIds;
    }

    public GcmNotification(GcmNotification copyNotification) {
        this(copyNotification.getRegistrationIds(),
                copyNotification.getCollapseKey(),
                copyNotification.getData(),
                copyNotification.getDelayWhileIdle(),
                copyNotification.getTtlSeconds(),
                copyNotification.getRestrictPackageName(),
                copyNotification.getDryRun());
    }

    public GcmNotification(JsonObject jsonObject) {
        this(new HashSet<>(jsonObject.getJsonArray(JSON_REG_IDS).getList()),
                jsonObject.getString(JSON_COLLAPSE_KEY),
                jsonObject.getJsonObject(JSON_DATA),
                jsonObject.getBoolean(JSON_DELAY_WHILE_IDLE),
                jsonObject.getLong(JSON_TTL_SECONDS),
                jsonObject.getString(JSON_RESTRICT_PACKAGE_NAME),
                jsonObject.getBoolean(JSON_DRY_RUN));
    }

    public GcmNotification(Set<String> registrationIds, String collapseKey, JsonObject data, Boolean delayWhileIdle, Long ttlSeconds, String restrictPackageName, Boolean dryRun) {
        this.registrationIds = registrationIds;
        this.collapseKey = collapseKey;
        this.data = data;
        this.delayWhileIdle = delayWhileIdle;
        this.ttlSeconds = ttlSeconds;
        this.restrictPackageName = restrictPackageName;
        this.dryRun = dryRun;
    }

    public GcmNotification checkState() throws IllegalStateException {
        if (registrationIds == null || registrationIds.isEmpty()) {
            throw new IllegalStateException("No registration IDs supplied. At least one registration ID required");
        }
        return this;
    }

    public Set<String> getRegistrationIds() {
        return registrationIds;
    }

    public GcmNotification setRegistrationIds(Set<String> registrationIds) {
        this.registrationIds = registrationIds;
        return this;
    }

    public String getCollapseKey() {
        return collapseKey;
    }

    public GcmNotification setCollapseKey(String collapseKey) {
        this.collapseKey = collapseKey;
        return this;
    }

    public JsonObject getData() {
        return data;
    }

    public GcmNotification setData(JsonObject data) {
        this.data = data;
        return this;
    }

    public Boolean getDelayWhileIdle() {
        return delayWhileIdle;
    }

    public GcmNotification setDelayWhileIdle(Boolean delayWhileIdle) {
        this.delayWhileIdle = delayWhileIdle;
        return this;
    }

    public Long getTtlSeconds() {
        return ttlSeconds;
    }

    public GcmNotification setTtlSeconds(Long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
        return this;
    }

    public String getRestrictPackageName() {
        return restrictPackageName;
    }

    public GcmNotification setRestrictPackageName(String restrictPackageName) {
        this.restrictPackageName = restrictPackageName;
        return this;
    }

    public Boolean getDryRun() {
        return dryRun;
    }

    public GcmNotification setDryRun(Boolean dryRun) {
        this.dryRun = dryRun;
        return this;
    }
}
