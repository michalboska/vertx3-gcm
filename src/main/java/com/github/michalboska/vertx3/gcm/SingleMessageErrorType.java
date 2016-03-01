package com.github.michalboska.vertx3.gcm;

/**
 * @author Michal Boska
 */

public enum SingleMessageErrorType {
    INVALID_REGISTRATION("InvalidRegistration"),
    NOT_REGISTERED("NotRegistered"),
    INVALID_PACKAGE_NAME("InvalidPackageName"),
    UNKNOWN("Unknown");

    private String errorCode;

    SingleMessageErrorType(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public static SingleMessageErrorType fromGoogleErrorCode(String errorCode) {
        for (SingleMessageErrorType type: SingleMessageErrorType.values()) {
            if (type.errorCode.equals(errorCode)) {
                return type;
            }
        }
        return UNKNOWN;
    }

}
