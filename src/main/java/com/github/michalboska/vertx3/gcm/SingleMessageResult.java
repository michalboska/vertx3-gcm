package com.github.michalboska.vertx3.gcm;


import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Describes result for a single device ID. GCM returns such result for each device ID the notification has been sent to.
 */
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
                .put(JSON_ERROR, error.getErrorCode());
    }

    /**
     * Whether the notification has been successfully queued for delivery on that device.
     * It doesn't mean the notification has really been delivered.
     * Also reply with canonical ID is considered successful.
     * @return
     */
    public Boolean getSuccess() {
        return this.error == null;
    }

    /**
     * Message ID that Google has assigned to this message
     * @return
     */
    public String getMessageId() {
        return messageId;
    }

    public SingleMessageResult setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    /**
     * New registration ID that should be used in the future instead of the original one used in this request.
     * @see <a href="https://developers.google.com/cloud-messaging/registration#canonical-ids">Canonical IDs</a>
     * @return
     */
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
        return SingleMessageErrorType.fromGoogleErrorCode(error);
    }
}
