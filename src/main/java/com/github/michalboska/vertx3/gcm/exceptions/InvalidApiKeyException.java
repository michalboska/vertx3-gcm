package com.github.michalboska.vertx3.gcm.exceptions;

/**
 * @author Michal Boska
 **/
public class InvalidApiKeyException extends GcmHttpException {

    public InvalidApiKeyException(String statusMessage) {
        super(String.format("Supplied API key was invalid. GCM sent 401: %s", statusMessage));
        this.statusCode = 401;
        this.statusString = statusMessage;
    }

}
