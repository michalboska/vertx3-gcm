package com.github.michalboska.vertx3.gcm.exceptions;

/**
 * @author Michal Boska
 **/
public class GcmException extends RuntimeException {

    public GcmException() {
    }

    public GcmException(String message) {
        super(message);
    }

    public GcmException(Throwable cause) {
        super(cause);
    }

    public GcmException(String message, Throwable cause) {
        super(message, cause);
    }

}
