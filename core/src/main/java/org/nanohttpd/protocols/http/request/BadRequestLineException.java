package org.nanohttpd.protocols.http.request;

public class BadRequestLineException extends BadHeaderException {

    public BadRequestLineException(String badLine, String message) {
        super(badLine, message);
    }

    public BadRequestLineException(String badLine, String message, Throwable cause) {
        super(badLine, message, cause);
    }

}
