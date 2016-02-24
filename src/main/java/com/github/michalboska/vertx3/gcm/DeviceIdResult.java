package com.github.michalboska.vertx3.gcm;


import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class DeviceIdResult {

    private Boolean success;
    private String messageId;
    private String registrationId;
    private GcmMessageError error;


    public DeviceIdResult() {
    }

    public DeviceIdResult(DeviceIdResult copyResult) {

    }

    public DeviceIdResult(JsonObject jsonObject) {

    }

    public DeviceIdResult(Boolean success, String messageId, String registrationId, GcmMessageError error) {
        this.success = success;
        this.messageId = messageId;
        this.registrationId = registrationId;
        this.error = error;
    }

    public JsonObject toJson() {
        return null;
    }

    public Boolean getSuccess() {
        return success;
    }

    public DeviceIdResult setSuccess(Boolean success) {
        this.success = success;
        return this;
    }

    public String getMessageId() {
        return messageId;
    }

    public DeviceIdResult setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public DeviceIdResult setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
        return this;
    }

    public GcmMessageError getError() {
        return error;
    }

    public DeviceIdResult setError(GcmMessageError error) {
        this.error = error;
        return this;
    }
}
