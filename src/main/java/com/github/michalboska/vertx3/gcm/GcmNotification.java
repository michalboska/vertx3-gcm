package com.github.michalboska.vertx3.gcm;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Michal Boska
 */
@DataObject
public class GcmNotification {

    private static String JSON_REG_IDS = "registration_ids";
    private static String JSON_COLLAPSE_KEY = "collapse_key";
    private static String JSON_DATA = "data";
    private static String JSON_DELAY_WHILE_IDLE = "delay_while_idle";
    private static String JSON_TTL_SECONDS = "ttl_seconds";
    private static String JSON_RESTRICT_PACKAGE_NAME = "restrict_package_name";
    private static String JSON_DRY_RUN = "dry_run";

    ///MANDATORY FIELDS
    //Set would be more appropriate, but Vert.x Json can't de/serialize Sets and we don't want to always convert
    //to/from Lists
    private List<String> registrationIds;

    ///OPTIONAL FIELDS
    private String collapseKey;
    private JsonObject data;
    private Boolean delayWhileIdle = false;
    private Long ttlSeconds;
    private String restrictPackageName;
    private Boolean dryRun = false;

    public GcmNotification() {
        this.registrationIds = Collections.emptyList();
    }

    public GcmNotification(List<String> registrationIds) {
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
        this(jsonObject.getJsonArray(JSON_REG_IDS).getList(),
                jsonObject.getString(JSON_COLLAPSE_KEY),
                jsonObject.getJsonObject(JSON_DATA),
                jsonObject.getBoolean(JSON_DELAY_WHILE_IDLE),
                jsonObject.getLong(JSON_TTL_SECONDS),
                jsonObject.getString(JSON_RESTRICT_PACKAGE_NAME),
                jsonObject.getBoolean(JSON_DRY_RUN));
    }

    public GcmNotification(List<String> registrationIds, String collapseKey, JsonObject data, Boolean delayWhileIdle, Long ttlSeconds, String restrictPackageName, Boolean dryRun) {
        this.registrationIds = registrationIds;
        this.collapseKey = collapseKey;
        this.data = data;
        this.delayWhileIdle = delayWhileIdle;
        this.ttlSeconds = ttlSeconds;
        this.restrictPackageName = restrictPackageName;
        this.dryRun = dryRun;
    }

    public JsonObject toJson() {
        JsonObject result = new JsonObject()
                .put(JSON_REG_IDS, new JsonArray(registrationIds));
        if (collapseKey != null) {
            result.put(JSON_COLLAPSE_KEY, collapseKey);
        }
        if (data != null) {
            result.put(JSON_DATA, data);
        }
        if (delayWhileIdle != null) {
            result.put(JSON_DELAY_WHILE_IDLE, delayWhileIdle);
        }
        if (ttlSeconds != null) {
            result.put(JSON_TTL_SECONDS, ttlSeconds);
        }
        if (restrictPackageName != null) {
            result.put(JSON_RESTRICT_PACKAGE_NAME, restrictPackageName);
        }
        if (dryRun != null) {
            result.put(JSON_DRY_RUN, dryRun);
        }
        return result;
    }

    /**
     * Creates a copy of this notification with modified device ID list
     *
     * @return
     */
    public GcmNotification copyWithDeviceIdList(Set<String> newDeviceIds) {
        return new GcmNotification(new ArrayList<>(newDeviceIds),
                collapseKey,
                data,
                delayWhileIdle,
                ttlSeconds,
                restrictPackageName,
                dryRun);
    }

    public GcmNotification checkState() throws IllegalStateException {
        if (registrationIds == null || registrationIds.isEmpty()) {
            throw new IllegalStateException("No registration IDs supplied. At least one registration ID required");
        }
        return this;
    }

    public List<String> getRegistrationIds() {
        return registrationIds;
    }

    public GcmNotification setRegistrationIds(List<String> registrationIds) {
        this.registrationIds = registrationIds;
        return this;
    }

    /**
     * A collapse key used to deliver multiple notifications of the same kind, of which only the latest is visible to the user.
     *
     * See <a href="https://developers.google.com/cloud-messaging/http-server-ref#send-downstream">GCM notification JSON syntax</a>
     * @return
     */
    public String getCollapseKey() {
        return collapseKey;
    }

    public GcmNotification setCollapseKey(String collapseKey) {
        this.collapseKey = collapseKey;
        return this;
    }

    /**
     * A custom payload that will be available to the application processing this message on the Android device.
     *
     * See <a href="https://developers.google.com/cloud-messaging/http-server-ref#send-downstream">GCM notification JSON syntax</a>
     * @return
     */
    public JsonObject getData() {
        return data;
    }

    public GcmNotification setData(JsonObject data) {
        this.data = data;
        return this;
    }

    /**
     * Only deliver the notification if the target device is active (waked, screen on)
     *
     * See <a href="https://developers.google.com/cloud-messaging/http-server-ref#send-downstream">GCM notification JSON syntax</a>
     * @return
     */
    public Boolean getDelayWhileIdle() {
        return delayWhileIdle;
    }

    public GcmNotification setDelayWhileIdle(Boolean delayWhileIdle) {
        this.delayWhileIdle = delayWhileIdle;
        return this;
    }

    /**
     * How long the message should be queued for delivery (at Google Servers) before discarding it as undeliverable, if delivery was not possible until then.
     *
     * See <a href="https://developers.google.com/cloud-messaging/http-server-ref#send-downstream">GCM notification JSON syntax</a>
     * @return
     */
    public Long getTtlSeconds() {
        return ttlSeconds;
    }

    public GcmNotification setTtlSeconds(Long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
        return this;
    }

    /**
     * Only send the notification to an application, whose package name matches this value
     *
     * See <a href="https://developers.google.com/cloud-messaging/http-server-ref#send-downstream">GCM notification JSON syntax</a>
     * @return
     */
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        GcmNotification that = (GcmNotification) o;

        return new EqualsBuilder()
                .append(registrationIds, that.registrationIds)
                .append(collapseKey, that.collapseKey)
                .append(data, that.data)
                .append(delayWhileIdle, that.delayWhileIdle)
                .append(ttlSeconds, that.ttlSeconds)
                .append(restrictPackageName, that.restrictPackageName)
                .append(dryRun, that.dryRun)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(registrationIds)
                .append(collapseKey)
                .append(data)
                .append(delayWhileIdle)
                .append(ttlSeconds)
                .append(restrictPackageName)
                .append(dryRun)
                .toHashCode();
    }
}
