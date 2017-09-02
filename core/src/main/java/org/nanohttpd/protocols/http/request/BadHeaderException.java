package org.nanohttpd.protocols.http.request;

public class BadHeaderException extends RequestException{


    private String line;

    public BadHeaderException(String badLine, String message) {
        super(message);
        this.line = badLine;
    }

    public BadHeaderException(String badLine, String message, Throwable cause) {
        super(message, cause);
        this.line = badLine;
    }

    public String getBadHeaderLine() {
        return line;
    }
}
