package com.github.michalboska.vertx3.gcm;


import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class SingleMessageResult {

    private static String JSON_MESSAGE_ID = "message_id";
    private static String JSON_REGISTRATION_ID = "registration_id";
    private static String JSON_ERROR = "error";

    private String messageId;
    private String registrationId;
    private SingleMessageErrorType error;


    public SingleMessageResult() {
    }

    public SingleMessageResult(SingleMessageResult copyResult) {
        this(copyResult.getMessageId(), copyResult.getRegistrationId(), copyResult.getError());
    }

    public SingleMessageResult(JsonObject jsonObject) {
        this(jsonObject.getString(JSON_MESSAGE_ID),
                jsonObject.getString(JSON_REGISTRATION_ID),
                toError(jsonObject.getString(JSON_ERROR)));
    }

    public SingleMessageResult(String messageId, String registrationId, SingleMessageErrorType error) {
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

    public SingleMessageResult setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public SingleMessageResult setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
        return this;
    }

    public SingleMessageErrorType getError() {
        return error;
    }

    public SingleMessageResult setError(SingleMessageErrorType error) {
        this.error = error;
        return this;
    }

    private static SingleMessageErrorType toError(String error) {
        if (error == null) {
            return null;
        }
        return SingleMessageErrorType.valueOf(error);
    }
}
