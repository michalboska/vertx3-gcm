package com.github.michalboska.vertx3.gcm;

/**
 * @author Michal Boska
 */

public enum SingleMessageErrorType {
    INVALID_REGISTRATION("InvalidRegistration", true, false),
    NOT_REGISTERED("NotRegistered", true, false),
    INVALID_PACKAGE_NAME("InvalidPackageName", false, false),
    MISMATCH_SENDER_ID("MismatchSenderId", false, false),
    MESSAGE_TOO_BIG("MessageTooBig", false, false),
    INVALID_DATA_KEY("InvalidDataKey", false, false),
    INVALID_TTL("InvalidTtl", false, false),
    DEVICE_MESSAGE_RATE_EXCEEDED("DeviceMessageRateExceeded", false, false),
    TOPICS_MESSAGE_RATE_EXCEEDED("TopicsMessageRateExceeded", false, false),
    UNAVAILABLE("Unavailable", false, true),
    INTERNAL_SERVER_ERROR("InternalServerError", false, true),
    UNKNOWN("Unknown", false, false);

    private String errorCode;
    private Boolean errorDueToWrongRegistrationId;
    private Boolean shouldRetry;

    SingleMessageErrorType(String errorCode, Boolean errorDueToWrongRegistrationId, Boolean shouldRetry) {
        this.errorCode = errorCode;
        this.errorDueToWrongRegistrationId = errorDueToWrongRegistrationId;
        this.shouldRetry = shouldRetry;
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

    /**
     * Determines, whether an error of this type could have been caused by a temporary GCM server problem
     * and whether it's worth to retry the request after some time
     *
     * @return
     */
    public Boolean shouldRetry() {
        return shouldRetry;
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
