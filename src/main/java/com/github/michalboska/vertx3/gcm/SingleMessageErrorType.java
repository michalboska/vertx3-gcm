package com.github.michalboska.vertx3.gcm;

/**
 * @author Michal Boska
 */

public enum SingleMessageErrorType {
    INVALID_REGISTRATION("InvalidRegistration", true),
    NOT_REGISTERED("NotRegistered", true),
    INVALID_PACKAGE_NAME("InvalidPackageName", false),
    MISMATCH_SENDER_ID("MismatchSenderId", false),
    MESSAGE_TOO_BIG("MessageTooBig", false),
    INVALID_DATA_KEY("InvalidDataKey", false),
    INVALID_TTL("InvalidTtl", false),
    INTERNAL_SERVER_ERROR("InternalServerError", false),
    DEVICE_MESSAGE_RATE_EXCEEDED("DeviceMessageRateExceeded", false),
    TOPICS_MESSAGE_RATE_EXCEEDED("TopicsMessageRateExceeded", false),
    UNKNOWN("Unknown", false);

    private String errorCode;
    private Boolean errorDueToWrongRegistrationId;

    SingleMessageErrorType(String errorCode, Boolean errorDueToWrongRegistrationId) {
        this.errorCode = errorCode;
        this.errorDueToWrongRegistrationId = errorDueToWrongRegistrationId;
    }

    /**
     * Gets the Google-specific error code
     *
     * @return
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Determines, whether this error type signalizes that the error was caused, because the supplied Registration ID was invalid
     * and hence that ID should be removed from the sender's databases
     *
     * @return True if the error was caused by invalid registration ID, false if the error was caused by some other reasons (e.g. technical difficulties)
     */
    public Boolean isErrorDueToWrongRegistrationId() {
        return errorDueToWrongRegistrationId;
    }

    public static SingleMessageErrorType fromGoogleErrorCode(String errorCode) {
        for (SingleMessageErrorType type : SingleMessageErrorType.values()) {
            if (type.errorCode.equals(errorCode)) {
                return type;
            }
        }
        return UNKNOWN;
    }

}
