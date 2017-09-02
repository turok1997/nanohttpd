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
import org.nanohttpd.util.IPushbackByteReader;

import java.io.*;
import java.util.StringTokenizer;

public class RequestLineParser {

    private InputStream requestReader;

    private StringTokenizer lineTokenizer;

    private int lastByte = -1;

    public RequestLineParser(InputStream inputStream) {
        this.requestReader = inputStream;
    }

    public HTTPRequestLine getRequestLine() throws BadHeaderException, IOException, EmptyHeaderException {

        String requestLine = readLine();
        if (requestLine == null)
            throw new EmptyHeaderException("BAD REQUEST. No request line. Seems the end of stream is reached");

        this.lineTokenizer = new StringTokenizer(requestLine);

        String methodInRequest = getMethod();

        String uri = getUri();

        String version = getHttpVersion();
        if (methodInRequest == null)
            throw new BadHeaderException(null, "BAD REQUEST: Missing request line (http method)");

        HTTPRequestLine httpRequestLine = new HTTPRequestLine(methodInRequest, uri, version);
        return httpRequestLine;
    }

    private String getMethod() {
        if (lineTokenizer.hasMoreTokens())
            return lineTokenizer.nextToken();
        return null;
    }

    private String getUri() {
        if (lineTokenizer.hasMoreTokens())
            return lineTokenizer.nextToken();
        return null;
    }

    private String getHttpVersion() {
        if (lineTokenizer.hasMoreTokens())
            return lineTokenizer.nextToken();
        return null;
    }

    private String readLine() throws IOException {
        int read = -1;
        StringBuilder sb = new StringBuilder(10);

        while ((read = requestReader.read()) != -1 && read != '\n') {
            sb.append((char) read);
        }
        if (read == -1 && sb.length() == 0)
            return null;
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\r')
            sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

}
