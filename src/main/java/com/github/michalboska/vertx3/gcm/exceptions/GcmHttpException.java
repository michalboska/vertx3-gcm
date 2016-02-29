package com.github.michalboska.vertx3.gcm.exceptions;

/**
 * @author Michal Boska
 **/
public class GcmHttpException extends GcmException {

    protected Integer statusCode;
    protected String statusString;
    protected String responseBody;

    public GcmHttpException(Integer statusCode, String statusString) {
        super(String.format("Server returned a HTTP %d error: %s", statusCode, statusString));
        this.statusCode = statusCode;
        this.statusString = statusString;
    }

    public GcmHttpException(Integer statusCode, String statusString, String responseBody) {
        this(statusCode, statusString);
        this.responseBody = responseBody;
    }

    public GcmHttpException(String message) {
        super(message);
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getStatusString() {
        return statusString;
    }

    public String getResponseBody() {
        return responseBody;
    }


}
