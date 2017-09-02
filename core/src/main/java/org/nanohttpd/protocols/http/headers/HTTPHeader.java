package org.nanohttpd.protocols.http.headers;

public class HTTPHeader {

    private String headerName;
    private String value;

    public HTTPHeader(String headerName, String value) {
        this.headerName = headerName;
        if ( this.headerName == null )
            throw new IllegalArgumentException("Header name cannot be null");

        this.value = value;
        if ( this.value == null )
            this.value = "";
    }

    public String getHeaderName() {
        return headerName;
    }

    public String getValue() {
        return value;
    }
}
