package org.nanohttpd.protocols.http.headers;

/*
 * #%L
 * NanoHttpd-Core
 * %%
 * Copyright (C) 2012 - 2017 nanohttpd
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the nanohttpd nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import org.nanohttpd.protocols.http.request.BadHeaderException;
import org.nanohttpd.protocols.http.request.BadRequestLineException;
import org.nanohttpd.protocols.http.request.Method;

public class HTTPRequestLine {

    private Method httpMethod;

    private String uri;

    private String httpVersion;

    private String requestLine;

    public HTTPRequestLine(String httpMethod, String uri, String httpVersion) throws BadHeaderException {

        this.requestLine = httpMethod + " " + uri + " " + httpVersion;
        this.httpMethod = Method.lookup(httpMethod);
        if (this.httpMethod == null)
            throw new BadRequestLineException(requestLine, "BAD REQUEST: Syntax error. " + httpMethod + " is not specified in RFC. ");

        this.uri = uri;
        if (uri == null)
            throw new BadHeaderException(null, "BAD REQUEST: Missing URI. Usage: GET /example/file.html");

        this.httpVersion = httpVersion;
        if (this.httpVersion == null)
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

    @Override
    public String toString() {
        return requestLine;
    }
}
