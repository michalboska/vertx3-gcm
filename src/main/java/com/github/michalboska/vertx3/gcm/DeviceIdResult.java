package com.github.michalboska.vertx3.gcm;


import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class DeviceIdResult {

    private static String JSON_MESSAGE_ID = "message_id";
    private static String JSON_REGISTRATION_ID = "registration_id";
    private static String JSON_ERROR = "error";

    private String messageId;
    private String registrationId;
    private GcmMessageError error;


    public DeviceIdResult() {
    }

    public DeviceIdResult(DeviceIdResult copyResult) {
        this(copyResult.getMessageId(), copyResult.getRegistrationId(), copyResult.getError());
    }

    public DeviceIdResult(JsonObject jsonObject) {
        this(jsonObject.getString(JSON_MESSAGE_ID),
                jsonObject.getString(JSON_REGISTRATION_ID),
                toError(jsonObject.getString(JSON_ERROR)));
    }

    public DeviceIdResult(String messageId, String registrationId, GcmMessageError error) {
        this.messageId = messageId;
        this.registrationId = registrationId;
        this.error = error;
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put(JSON_MESSAGE_ID, messageId)
                .put(JSON_REGISTRATION_ID, registrationId)
                .put(JSON_ERROR, error.name());
    }

    public Boolean getSuccess() {
        return this.error == null;
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

    private static GcmMessageError toError(String error) {
        if (error == null) {
            return null;
        }
        return GcmMessageError.valueOf(error);
    }
}
