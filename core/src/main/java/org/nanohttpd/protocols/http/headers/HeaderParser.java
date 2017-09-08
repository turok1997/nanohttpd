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

import org.nanohttpd.protocols.http.HTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.request.BadHeaderException;
import org.nanohttpd.protocols.http.response.Status;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class HeaderParser {

    private PushbackInputStream inputStream;

    private final static int CRLF_LENGTH = 2;

    private final static int HTTP_METHOD_AVERAGE_LENGTH = 4;

    private final byte[] CRLF_BUF = new byte[CRLF_LENGTH];

    private int headersCount = 0;

    private int headerSizeInBytes = 0;

    private int maxHeadersSize;

    public HeaderParser(InputStream markableInputStream) {
        this(markableInputStream, HTTPSession.MAX_HEADER_SIZE);
    }

    public HeaderParser(InputStream markableInputStream, int headerSizeLimit) {
        this.inputStream = new PushbackInputStream(markableInputStream, CRLF_LENGTH);
        this.maxHeadersSize = headerSizeLimit;
    }

    public boolean hasNext() throws IOException, NanoHTTPD.ResponseException {
        int read = inputStream.read(CRLF_BUF);
        if (read == -1)
            return false;

        boolean hasNext = true;

        if (CRLF_BUF[0] == '\n') {
            hasNext = false;
            inputStream.unread(CRLF_BUF, 1, read - 1);
            headerSizeInBytes++;
            if (headerSizeInBytes > maxHeadersSize)
                throw new NanoHTTPD.ResponseException(Status.INTERNAL_ERROR, "Multipart header size exceeds MAX_HEADER_SIZE.");
        } else if (read == 2 && CRLF_BUF[0] == '\r' && CRLF_BUF[1] == '\n') {
            hasNext = false;
            headerSizeInBytes += 2;
            if (headerSizeInBytes > maxHeadersSize)
                throw new NanoHTTPD.ResponseException(Status.INTERNAL_ERROR, "Multipart header size exceeds MAX_HEADER_SIZE.");
        }

        if (hasNext) {
            inputStream.unread(CRLF_BUF, 0, read);
        }

        return hasNext;
    }

    public HTTPHeader next() throws IOException, BadHeaderException, NanoHTTPD.ResponseException {
        String headerName = getHeaderName();
        String value = getHeaderValue();

        return new HTTPHeader(headerName, value);
    }

    private String getHeaderName() throws IOException, BadHeaderException, NanoHTTPD.ResponseException {

        StringBuilder headerName = new StringBuilder(HTTP_METHOD_AVERAGE_LENGTH);

        int read = -1;
        while ((read = inputStream.read()) != -1 && ((char) read) != ':' && ((char) read) != '\n') {
            headerName.append((char) read);
            headerSizeInBytes++;
            if (headerSizeInBytes > maxHeadersSize)
                throw new NanoHTTPD.ResponseException(Status.INTERNAL_ERROR, "Multipart header size exceeds MAX_HEADER_SIZE.");
        }

        headerSizeInBytes += read == -1 ? 0 : 1;

        if (read == -1 || read == '\n') {
            throw new BadHeaderException(headerName.toString(), "BAD REQUEST: Syntax error. Header " + headerName + " is not complete. Example HeaderName: Value");
        }

        return headerName.toString().trim();
    }

    private String getHeaderValue() throws IOException, NanoHTTPD.ResponseException {
        StringBuilder value = new StringBuilder();

        int read = -1;
        while ((read = inputStream.read()) != -1 && ((char) read) != '\n') {
            value.append((char) read);
            headerSizeInBytes++;
            if (headerSizeInBytes > maxHeadersSize)
                throw new NanoHTTPD.ResponseException(Status.INTERNAL_ERROR, "Multipart header size exceeds MAX_HEADER_SIZE.");
        }

        headerSizeInBytes += read == -1 ? 0 : 1;

        if (value.length() > 0 && value.charAt(value.length() - 1) == '\r')
            value.deleteCharAt(value.length() - 1);

        return value.toString().trim();
    }

    private boolean isEnglishLetter(char c) {

        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    public int getCurrentHeaderSize() {
        return headerSizeInBytes;
    }
}
