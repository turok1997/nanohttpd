package org.nanohttpd.protocols.http.headers;

public class EmptyHeaderException extends Exception {

    public EmptyHeaderException(String message) {
        super(message);
    }

    public EmptyHeaderException(String message, Throwable cause) {
        super(message, cause);
    }
}
