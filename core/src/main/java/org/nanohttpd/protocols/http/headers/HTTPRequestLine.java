package org.nanohttpd.protocols.http.headers;

import org.nanohttpd.protocols.http.request.BadHeaderException;
import org.nanohttpd.protocols.http.request.BadRequestLineException;
import org.nanohttpd.protocols.http.request.Method;

public class HTTPRequestLine {
    private Method httpMethod;
    private String uri;
    private String httpVersion;

    public HTTPRequestLine(String httpMethod, String uri, String httpVersion) throws BadHeaderException {

        String requestLine = httpMethod + " " + uri + " " + httpVersion;
        this.httpMethod = Method.lookup(httpMethod);
        if ( this.httpMethod == null )
            throw new BadRequestLineException(requestLine,
                    "BAD REQUEST: Syntax error. " +
                            httpMethod + " is not specified in RFC. ");

        this.uri = uri;
        if ( uri == null )
            throw new BadHeaderException(null, "BAD REQUEST: Missing URI. Usage: GET /example/file.html");

        this.httpVersion = httpVersion;
        if ( this.httpVersion == null )
            httpVersion = "HTTP/1.1";
    }

    public Method getHttpMethod() {
        return httpMethod;
    }

    public String getUri() {
        return uri;
    }

    public String getHttpVersion() {
        return httpVersion;
    }
}
