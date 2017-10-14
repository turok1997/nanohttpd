package org.nanohttpd.protocols.http;

/*
 * #%L
 * NanoHttpd-Core
 * %%
 * Copyright (C) 2012 - 2016 nanohttpd
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

import java.io.*;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.cert.CRL;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;

import javax.net.ssl.SSLException;

import org.nanohttpd.protocols.http.NanoHTTPD.ResponseException;
import org.nanohttpd.protocols.http.content.ContentType;
import org.nanohttpd.protocols.http.content.CookieHandler;
import org.nanohttpd.protocols.http.headers.*;
import org.nanohttpd.protocols.http.request.BadHeaderException;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;
import org.nanohttpd.protocols.http.tempfiles.ITempFile;
import org.nanohttpd.protocols.http.tempfiles.ITempFileManager;
import org.nanohttpd.util.LineReader;

public class HTTPSession implements IHTTPSession {

    private int count = 0;

    public static final String POST_DATA = "postData";

    private static final int REQUEST_BUFFER_LEN = 512;

    private static final int MEMORY_STORE_LIMIT = 1024;

    public static final int BUFSIZE = 8192;

    public static final int MAX_HEADER_SIZE = 1024;

    private final NanoHTTPD httpd;

    private final ITempFileManager tempFileManager;

    private final OutputStream outputStream;

    private final BufferedInputStream inputStream;

    private int splitbyte;

    private int rlen;

    private String uri;

    private Method method;

    private Map<String, List<String>> parms;

    private int[] overlap;

    private Map<String, String> headers;

    private Map<String, String> files;

    private CookieHandler cookies;

    private String queryParameterString;

    private boolean newBoundaryFound = false;

    private String remoteIp;

    private String remoteHostname;

    private String protocolVersion;

    private long occupiedBodyDataMemory;

    private long lastLineSize = 0;

    private long bodySize = 0;

    public HTTPSession(NanoHTTPD httpd, ITempFileManager tempFileManager, InputStream inputStream, OutputStream outputStream) {
        this.httpd = httpd;
        this.tempFileManager = tempFileManager;
        this.inputStream = new BufferedInputStream(inputStream, HTTPSession.BUFSIZE);
        this.outputStream = outputStream;
    }

    public HTTPSession(NanoHTTPD httpd, ITempFileManager tempFileManager, InputStream inputStream, OutputStream outputStream, InetAddress inetAddress) {
        this.httpd = httpd;
        this.tempFileManager = tempFileManager;
        this.inputStream = new BufferedInputStream(inputStream, HTTPSession.BUFSIZE);
        this.outputStream = outputStream;
        this.remoteIp = inetAddress.isLoopbackAddress() || inetAddress.isAnyLocalAddress() ? "127.0.0.1" : inetAddress.getHostAddress().toString();
        this.remoteHostname = inetAddress.isLoopbackAddress() || inetAddress.isAnyLocalAddress() ? "localhost" : inetAddress.getHostName().toString();
        this.headers = new HashMap<String, String>();
        this.files = new HashMap<>();
    }

    /**
     * Decodes the sent headers and loads the data into Key/value pairs
     */
    private String parseUri(Map<String, List<String>> filledMap, String uri) {
        // Decode parameters from the URI
        int qmi = uri.indexOf('?');

        if (qmi >= 0) {
            decodeParms(uri.substring(qmi + 1), filledMap);
            return NanoHTTPD.decodePercent(uri.substring(0, qmi));
        }

        return NanoHTTPD.decodePercent(uri);
    }

    private long getFreeMemory() {
        return Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
    }

    private void decodeMultipartFormData(InputStream is, ContentType contentType, Map<String, List<String>> parms, Map<String, String> files) throws IOException,
            ResponseException {
        if (bodySize <= 0)
            return;
        int pcount = 0;
        byte[] CRLF_BUF = new byte[2];
        byte[] boundary = contentType.getBoundary().getBytes();
        HeaderParser headerParser = new HeaderParser(is);

        // first line of body should be a boundary
        String firstBoundary = readLine(boundary.length);
        if (firstBoundary == null || !firstBoundary.substring(2).equals(contentType.getBoundary()))
            throw new ResponseException(Status.BAD_REQUEST, "BAD REQUEST: Content type is multipart/form-data but doesn't start from a boundary");
        bodySize -= lastLineSize;
        System.out.println(firstBoundary);
        newBoundaryFound = true;

        while (newBoundaryFound) {
            is.mark(2);
            int read = is.read(CRLF_BUF, 0, 2);
            if (read == -1)
                break;
            is.reset();
            String partName = null;
            String fileName = null;
            String partContentType = null;
            int headerCount = 0;
            while (headerParser.hasNext()) {
                HTTPHeader header = null;
                try {
                    header = headerParser.next();
                    System.out.println(header);
                    ++headerCount;
                } catch (BadHeaderException e) {
                    continue;
                }
                Matcher matcher = NanoHTTPD.CONTENT_DISPOSITION_PATTERN.matcher(header.toString());

                if (matcher.matches()) {
                    String attributeString = header.getValue();

                    matcher = NanoHTTPD.CONTENT_DISPOSITION_ATTRIBUTE_PATTERN.matcher(attributeString);
                    while (matcher.find()) {
                        String key = matcher.group(1);
                        if ("name".equalsIgnoreCase(key)) {
                            partName = matcher.group(2);
                        } else if ("filename".equalsIgnoreCase(key)) {
                            fileName = matcher.group(2);
                            // add these two line to support multiple
                            // files uploaded using the same field Id
                            if (!fileName.isEmpty()) {
                                if (pcount > 0)
                                    partName = partName + String.valueOf(pcount++);
                                else
                                    ++pcount;
                            }
                        }
                    }
                }

                matcher = NanoHTTPD.CONTENT_TYPE_PATTERN.matcher(header.toString());
                if (matcher.matches()) {
                    partContentType = matcher.group(2).trim();
                }
            }

            bodySize -= headerParser.getCurrentHeaderSize();

            if (partName == null)
                throw new NanoHTTPD.ResponseException(Status.BAD_REQUEST, "Multipart form data has a part with unknown field name");
            if (fileName != null) {

            }
            // read actual data
            List<String> values = parms.get(partName);
            if (values == null) {
                values = new ArrayList<String>();
                parms.put(partName, values);
            }

            if (partContentType == null) {
                // Read the part into a string
                values.add(readPartPlainData(boundary));
            } else {
                // Read it into a file
                String path = saveBodyPartIntoTempFile(is, boundary, fileName);
                if (!files.containsKey(partName)) {
                    files.put(partName, path);
                } else {
                    int count = 2;
                    while (files.containsKey(partName + count)) {
                        count++;
                    }
                    files.put(partName + count, path);
                }
                values.add(fileName);
            }

            // firstBoundary = readLine(boundary.length);
            // if ( firstBoundary == null )
            // break;
            // bodySize -= lastLineSize;
        }

    }

    private String saveBodyPartIntoTempFile(InputStream is, byte[] boundary, String filename) {

        OutputStream fileOutputStream = null;
        long total = 0;
        try {
            ITempFile tempFile = this.tempFileManager.createTempFile(filename);
            fileOutputStream = tempFile.open();
            byte[] buf = new byte[BUFSIZE];
            int bufLength = 0;
            byte[] possibleDelimiter = new byte[4];
            int possibleDelimiterLength = 0;

            int read = 1;
            int k = 0;

            // System.out.println("Starting reading. COntent size: " +
            // bodySize);
            long startTime = System.currentTimeMillis();
            while (bodySize > 0 && read > 0) {
                if (k == 0)
                    is.mark(BUFSIZE);
                // System.out.println("\n################################");
                read = is.read(buf, k, (int) Math.min(bodySize, buf.length - k));
                bufLength = k + read;
                // System.out.println("Chunk of size: " + read + " left size: "
                // + bodySize);
                // System.out.println("Read " + read + " new bytes");
                // System.out.println(new String(buf, 0, bufLength));

                bodySize -= read;

                int o = 0;
                int bufIndex, patternIndex = o;
                for (bufIndex = 0; bufIndex < bufLength;) {

                    for (patternIndex = o; patternIndex < boundary.length && bufIndex + patternIndex < bufLength && buf[bufIndex + patternIndex] == boundary[patternIndex]; patternIndex++)
                        ;

                    if (patternIndex == boundary.length) {
                        int impossibleDelimiterLength = Math.min(bufIndex, possibleDelimiterLength);

                        // write byte that is impossible for delimiter
                        fileOutputStream.write(possibleDelimiter, 0, impossibleDelimiterLength);
                        total += impossibleDelimiterLength;
                        // move possible delimiters to left
                        System.arraycopy(possibleDelimiter, impossibleDelimiterLength, possibleDelimiter, 0, possibleDelimiterLength - impossibleDelimiterLength);
                        // new length of possible delimiters
                        possibleDelimiterLength = possibleDelimiterLength - impossibleDelimiterLength;
                        // fhjghjg if have empty place fill with possible
                        // delimiter
                        // int emptySpace = possibleDelimiter.length -
                        // possibleDelimiterLength;
                        fileOutputStream.write(buf, 0, Math.max(0, bufIndex - possibleDelimiter.length));
                        total += Math.max(0, bufIndex - possibleDelimiter.length);
                        // System.arraycopy(buf, bufIndex-emptySpace,
                        // possibleDelimiter, possibleDelimiterLength,
                        // emptySpace);

                        // System.out.println("Boundary found at i=" + bufIndex
                        // + " and j = " + patternIndex + " Already written: " +
                        // total);
                        // fileOutputStream.write(buf, emptySpace,
                        // bufIndex-emptySpace);

                        is.reset();
                        is.skip(bufIndex + patternIndex);
                        bodySize += read - (bufIndex + patternIndex - k);

                        // System.out.println("total written: " + total +
                        // " . LEft size: " + bodySize);

                        byte[] CRLF = new byte[4];
                        int crlf_read = is.read(CRLF, 0, CRLF.length);
                        bodySize -= crlf_read;
                        if (crlf_read != 4 || CRLF[0] != '-' || CRLF[1] != '-')
                            throw new ResponseException(Status.BAD_REQUEST, ":(");
                        newBoundaryFound = false;
                        long finishtTime = System.currentTimeMillis();
                        // System.out.println("Total time: " + (finishtTime -
                        // startTime));
                        return tempFile.getName();
                    }

                    if (bufIndex + patternIndex == bufLength) {

                        if (possibleDelimiterLength > 0) {
                            fileOutputStream.write(possibleDelimiter, 0, possibleDelimiterLength);
                            total += possibleDelimiterLength;
                            possibleDelimiterLength = 0;
                        }

                        fileOutputStream.write(buf, 0, bufIndex - possibleDelimiter.length);
                        total += bufIndex - possibleDelimiter.length;
                        // System.out.println((bufIndex-possibleDelimiter.length)
                        // + " bytes written into file");

                        for (possibleDelimiterLength = 0; possibleDelimiterLength < possibleDelimiter.length; possibleDelimiterLength++)
                            possibleDelimiter[possibleDelimiterLength] = buf[bufIndex - possibleDelimiter.length + possibleDelimiterLength];
                        System.arraycopy(buf, bufIndex, buf, 0, patternIndex);

                        // System.out.println("Possible delimiter: " + new
                        // String(possibleDelimiter, 0,
                        // possibleDelimiterLength));
                        is.reset();
                        is.skip(bufIndex);
                        is.mark(BUFSIZE);
                        is.skip(patternIndex);

                        // System.out.println(new String(buf, 0, i));
                        // System.out.println("omg this happened. " + bufIndex +
                        // " will start at zero with j " + patternIndex);
                        // System.out.println("buf[" + bufIndex + "] = " + new
                        // String(buf, bufIndex, patternIndex));
                        // System.out.println("Content size: " + bodySize);

                        o = k = patternIndex;
                        break;
                    }

                    o = Math.max(0, overlap[patternIndex]);
                    bufIndex = bufIndex + Math.max(1, patternIndex - o);
                }

                if (bufIndex == bufLength) {
                    // if ( bufLength == 1 )
                    // System.out.println("yuppi");
                    int impossibleDelimiterLength = Math.min(bufIndex, possibleDelimiterLength);

                    // write byte that is impossible for delimiter
                    fileOutputStream.write(possibleDelimiter, 0, impossibleDelimiterLength);
                    total += impossibleDelimiterLength;
                    // move possible delimiters to left
                    System.arraycopy(possibleDelimiter, impossibleDelimiterLength, possibleDelimiter, 0, possibleDelimiterLength - impossibleDelimiterLength);
                    // new length of possible delimiters
                    possibleDelimiterLength = possibleDelimiterLength - impossibleDelimiterLength;
                    // if have empty place, fill with possible delimiter
                    int emptySpace = possibleDelimiter.length - possibleDelimiterLength;

                    fileOutputStream.write(buf, 0, Math.max(0, bufIndex - possibleDelimiter.length));
                    total += Math.max(0, bufIndex - possibleDelimiter.length);
                    System.arraycopy(buf, bufIndex - emptySpace, possibleDelimiter, possibleDelimiterLength, emptySpace);
                    possibleDelimiterLength += emptySpace;
                    // System.out.println(buf.length-possibleDelimiter.length +
                    // " bytes written into file");

                    // save possible delimiters
                    // System.arraycopy(buf, bufLength-possibleDelimiter.length,
                    // possibleDelimiter, 0, possibleDelimiter.length);
                    // possibleDelimiterLength = possibleDelimiter.length;

                    k = 0;
                    o = 0;
                    // System.out.print(new String(buf, 0, read));
                }
            }
            newBoundaryFound = false;
            // System.out.println("happy end: " + total);
            return null;

            // fileOutputStream.write(buf, 0, k);
        } catch (Exception error) {
            throw new Error(error);
        } finally {
            NanoHTTPD.safeClose(fileOutputStream);
        }

    }

    private String readPartPlainData(byte[] boundary) throws IOException, ResponseException {
        int read = -1;
        int j = 0;
        StringBuilder sb = new StringBuilder((int) Math.max(0, getAvailableDataMemory()));
        inputStream.mark(boundary.length);
        while ((read = this.inputStream.read()) != -1) {
            sb.append((char) read);
            occupiedBodyDataMemory++;
            if (occupiedBodyDataMemory > MEMORY_STORE_LIMIT + boundary.length)
                throw new ResponseException(Status.INTERNAL_ERROR, "Not enough memory");
            while (true) {
                if (boundary[j] == read) {
                    j++;
                    if (j == boundary.length) {
                        j = overlap[j - 1];
                        inputStream.reset();
                        occupiedBodyDataMemory -= boundary.length;
                        return sb.delete(sb.length() - boundary.length - 4, boundary.length + 4).toString();
                    }
                    break;
                }

                if (j == 0)
                    break;

                inputStream.reset();
                inputStream.skip(Math.max(1, j + 1 - overlap[j]));
                inputStream.mark(boundary.length);
                j = overlap[j];
                inputStream.skip(j);
            }
        }

        return sb.toString();
    }

    private long getAvailableDataMemory() {
        return Math.min(MEMORY_STORE_LIMIT - occupiedBodyDataMemory, getFreeMemory());
    }

    private void calcOverlap(byte[] pattern) {
        overlap = new int[pattern.length + 1];
        if (pattern.length == 0)
            return;

        overlap[0] = -1;

        for (int i = 0; i < pattern.length; i++) {
            overlap[i + 1] = overlap[i] + 1;
            while (overlap[i + 1] > 0 && pattern[i] != pattern[overlap[i + 1] - 1])
                overlap[i + 1] = overlap[overlap[i + 1] - 1] + 1;
        }
    }

    /**
     * Decodes the Multipart Body data and put it into Key/Value pairs.
     */
    private void decodeMultipartFormData(ContentType contentType, ByteBuffer fbuf, Map<String, List<String>> parms, Map<String, String> files) throws ResponseException {
        int pcount = 0;
        try {
            int[] boundaryIdxs = getBoundaryPositions(fbuf, contentType.getBoundary().getBytes());
            if (boundaryIdxs.length < 2) {
                throw new ResponseException(Status.BAD_REQUEST, "BAD REQUEST: Content type is multipart/form-data but contains less than two boundary strings.");
            }

            byte[] partHeaderBuff = new byte[MAX_HEADER_SIZE];

            for (int boundaryIdx = 0; boundaryIdx < boundaryIdxs.length - 1; boundaryIdx++) {
                fbuf.position(boundaryIdxs[boundaryIdx]);
                int len = (fbuf.remaining() < MAX_HEADER_SIZE) ? fbuf.remaining() : MAX_HEADER_SIZE;
                fbuf.get(partHeaderBuff, 0, len);
                BufferedReader in =
                        new BufferedReader(new InputStreamReader(new ByteArrayInputStream(partHeaderBuff, 0, len), Charset.forName(contentType.getEncoding())), len);
                int headerLines = 0;
                // First line is boundary string
                String mpline = in.readLine();
                headerLines++;
                if (mpline == null || !mpline.contains(contentType.getBoundary())) {
                    throw new ResponseException(Status.BAD_REQUEST, "BAD REQUEST: Content type is multipart/form-data but chunk does not start with boundary.");
                }

                String partName = null, fileName = null, partContentType = null;
                // Parse the reset of the header lines
                mpline = in.readLine();
                headerLines++;
                while (mpline != null && mpline.trim().length() > 0) {
                    Matcher matcher = NanoHTTPD.CONTENT_DISPOSITION_PATTERN.matcher(mpline);
                    if (matcher.matches()) {
                        String attributeString = matcher.group(2);
                        matcher = NanoHTTPD.CONTENT_DISPOSITION_ATTRIBUTE_PATTERN.matcher(attributeString);
                        while (matcher.find()) {
                            String key = matcher.group(1);
                            if ("name".equalsIgnoreCase(key)) {
                                partName = matcher.group(2);
                            } else if ("filename".equalsIgnoreCase(key)) {
                                fileName = matcher.group(2);
                                // add these two line to support multiple
                                // files uploaded using the same field Id
                                if (!fileName.isEmpty()) {
                                    if (pcount > 0)
                                        partName = partName + String.valueOf(pcount++);
                                    else
                                        pcount++;
                                }
                            }
                        }
                    }
                    matcher = NanoHTTPD.CONTENT_TYPE_PATTERN.matcher(mpline);
                    if (matcher.matches()) {
                        partContentType = matcher.group(2).trim();
                    }
                    mpline = in.readLine();
                    headerLines++;
                }
                int partHeaderLength = 0;
                while (headerLines-- > 0) {
                    partHeaderLength = scipOverNewLine(partHeaderBuff, partHeaderLength);
                }
                // Read the part data
                if (partHeaderLength >= len - 4) {
                    throw new ResponseException(Status.INTERNAL_ERROR, "Multipart header size exceeds MAX_HEADER_SIZE.");
                }
                int partDataStart = boundaryIdxs[boundaryIdx] + partHeaderLength;
                int partDataEnd = boundaryIdxs[boundaryIdx + 1] - 4;

                fbuf.position(partDataStart);

                List<String> values = parms.get(partName);
                if (values == null) {
                    values = new ArrayList<String>();
                    parms.put(partName, values);
                }

                if (partContentType == null) {
                    // Read the part into a string
                    byte[] data_bytes = new byte[partDataEnd - partDataStart];
                    fbuf.get(data_bytes);

                    values.add(new String(data_bytes, contentType.getEncoding()));
                } else {
                    // Read it into a file
                    String path = saveTmpFile(fbuf, partDataStart, partDataEnd - partDataStart, fileName);
                    if (!files.containsKey(partName)) {
                        files.put(partName, path);
                    } else {
                        int count = 2;
                        while (files.containsKey(partName + count)) {
                            count++;
                        }
                        files.put(partName + count, path);
                    }
                    values.add(fileName);
                }
            }
        } catch (ResponseException re) {
            throw re;
        } catch (Exception e) {
            throw new ResponseException(Status.INTERNAL_ERROR, e.toString());
        }
    }

    private String readLine(int lengthHint) throws IOException {
        int read = -1;
        lastLineSize = 0;
        StringBuilder sb = new StringBuilder(Math.max(lengthHint, 0));

        while ((read = this.inputStream.read()) != -1 && read != '\n') {
            sb.append((char) read);
            ++lastLineSize;
        }

        if (read == '\n')
            ++lastLineSize;

        if (read == -1 && sb.length() == 0)
            return null;

        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\r')
            sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    private int scipOverNewLine(byte[] partHeaderBuff, int index) {
        while (partHeaderBuff[index] != '\n') {
            index++;
        }
        return ++index;
    }

    /**
     * Decodes parameters in percent-encoded URI-format ( e.g.
     * "name=Jack%20Daniels&pass=Single%20Malt" ) and adds them to given Map.
     */
    private void decodeParms(String parms, Map<String, List<String>> p) {
        if (parms == null) {
            this.queryParameterString = "";
            return;
        }

        this.queryParameterString = parms;
        StringTokenizer st = new StringTokenizer(parms, "&");
        while (st.hasMoreTokens()) {
            String e = st.nextToken();
            int sep = e.indexOf('=');
            String key = null;
            String value = null;

            if (sep >= 0) {
                key = NanoHTTPD.decodePercent(e.substring(0, sep)).trim();
                value = NanoHTTPD.decodePercent(e.substring(sep + 1));
            } else {
                key = NanoHTTPD.decodePercent(e).trim();
                value = "";
            }

            List<String> values = p.get(key);
            if (values == null) {
                values = new ArrayList<String>();
                p.put(key, values);
            }

            values.add(value);
        }
    }

    private void readRequestLine(Map<String, String> filledMap, InputStream is) throws IOException, BadHeaderException, EmptyHeaderException {
        RequestLineParser requestLineParser = new RequestLineParser(is);
        HTTPRequestLine requestLine = requestLineParser.getRequestLine();
        filledMap.put("method", requestLine.getHttpMethod().toString());
        filledMap.put("uri", requestLine.getUri());
        filledMap.put("http-version", requestLine.getHttpVersion());
        System.out.println(requestLine);
    }

    private void readHeaders(Map<String, String> filledMap, InputStream is) throws IOException, ResponseException {
        HeaderParser headerParser = new HeaderParser(is);
        HTTPHeader header = null;

        while (headerParser.hasNext()) {
            try {
                header = headerParser.next();
            } catch (BadHeaderException e) {
                continue;
            }

            filledMap.put(header.getHeaderName().toLowerCase(Locale.US), header.getValue());
            System.out.println(header.getHeaderName() + ": " + header.getValue());
        }
    }

    @Override
    public void execute() throws IOException {
        Response r = null;
        Map<String, String> pre = new HashMap<String, String>();
        this.parms = new HashMap<String, List<String>>();
        if (null == this.headers) {
            this.headers = new HashMap<String, String>();
        } else {
            this.headers.clear();
        }
        // PushbackInputStream pis = new PushbackInputStream(this.inputStream);

        try {
            try {
                readRequestLine(pre, this.inputStream);
                this.method = Method.lookup(pre.get("method"));
                this.protocolVersion = pre.get("http-version");

                readHeaders(this.headers, this.inputStream);
                this.uri = parseUri(this.parms, pre.get("uri"));
                pre.put("uri", this.uri);
            } catch (SSLException e) {
                throw e;
            } catch (IOException e) {
                NanoHTTPD.safeClose(this.inputStream);
                NanoHTTPD.safeClose(this.outputStream);
                throw new SocketException("NanoHttpd Shutdown");
            } catch (EmptyHeaderException e) {
                NanoHTTPD.safeClose(this.inputStream);
                NanoHTTPD.safeClose(this.outputStream);
                throw new SocketException("NanoHttpd Shutdown");
            } catch (BadHeaderException e) {
                throw new ResponseException(Status.BAD_REQUEST, e.getMessage() + ". Bad line: " + e.getBadHeaderLine());
            }
            count++;

            if (null != this.remoteIp) {
                this.headers.put("remote-addr", this.remoteIp);
                this.headers.put("http-client-ip", this.remoteIp);
            }

            this.cookies = new CookieHandler(this.headers);

            String connection = this.headers.get("connection");
            boolean keepAlive = "HTTP/1.1".equals(protocolVersion) && (connection == null || !connection.matches("(?i).*close.*"));

            // Ok, now do the serve()

            // TODO: long body_size = getBodySize();
            // TODO: long pos_before_serve = this.inputStream.totalRead()
            // (requires implementation for totalRead())
            r = httpd.handle(this);
            // TODO: this.inputStream.skip(body_size -
            // (this.inputStream.totalRead() - pos_before_serve))

            if (r == null) {
                throw new ResponseException(Status.INTERNAL_ERROR, "SERVER INTERNAL ERROR: Serve() returned a null response.");
            } else {
                String acceptEncoding = this.headers.get("accept-encoding");
                this.cookies.unloadQueue(r);
                r.setRequestMethod(this.method);
                if (acceptEncoding == null || !acceptEncoding.contains("gzip")) {
                    r.setUseGzip(false);
                }
                r.setKeepAlive(keepAlive);
                r.send(this.outputStream);
            }
            if (!keepAlive || r.isCloseConnection()) {
                throw new SocketException("NanoHttpd Shutdown");
            }
        } catch (SocketException e) {
            // throw it out to close socket object (finalAccept)
            throw e;
        } catch (SSLException ssle) {
            Response resp = Response.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "SSL PROTOCOL FAILURE: " + ssle.getMessage());
            resp.send(this.outputStream);
            NanoHTTPD.safeClose(this.outputStream);
        } catch (IOException ioe) {
            Response resp = Response.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            resp.send(this.outputStream);
            NanoHTTPD.safeClose(this.outputStream);
        } catch (ResponseException re) {
            Response resp = Response.newFixedLengthResponse(re.getStatus(), NanoHTTPD.MIME_PLAINTEXT, re.getMessage());
            resp.send(this.outputStream);
            NanoHTTPD.safeClose(this.outputStream);
        } finally {
            NanoHTTPD.safeClose(r);
            this.tempFileManager.clear();
        }
    }

    /**
     * Find byte index separating header from body. It must be the last byte of
     * the first two sequential new lines.
     */
    private int findHeaderEnd(final byte[] buf, int rlen) {
        for (int splitbyte = 0; splitbyte + 1 < rlen; splitbyte++) {

            // RFC2616
            if (buf[splitbyte] == '\r' && buf[splitbyte + 1] == '\n' && splitbyte + 3 < rlen && buf[splitbyte + 2] == '\r' && buf[splitbyte + 3] == '\n') {
                return splitbyte + 4;
            }

            // tolerance
            if (buf[splitbyte] == '\n' && buf[splitbyte + 1] == '\n') {
                return splitbyte + 2;
            }

        }
        return 0;
    }

    /**
     * Find the byte positions where multipart boundaries start. This reads a
     * large block at a time and uses a temporary buffer to optimize (memory
     * mapped) file access.
     */
    private int[] getBoundaryPositions(ByteBuffer b, byte[] boundary) {
        int[] res = new int[0];
        if (b.remaining() < boundary.length) {
            return res;
        }

        int search_window_pos = 0;
        byte[] search_window = new byte[4 * 1024 + boundary.length];

        int first_fill = (b.remaining() < search_window.length) ? b.remaining() : search_window.length;
        b.get(search_window, 0, first_fill);
        int new_bytes = first_fill - boundary.length;

        do {
            // Search the search_window
            for (int j = 0; j < new_bytes; j++) {
                for (int i = 0; i < boundary.length; i++) {
                    if (search_window[j + i] != boundary[i])
                        break;
                    if (i == boundary.length - 1) {
                        // Match found, add it to results
                        int[] new_res = new int[res.length + 1];
                        System.arraycopy(res, 0, new_res, 0, res.length);
                        new_res[res.length] = search_window_pos + j;
                        res = new_res;
                    }
                }
            }
            search_window_pos += new_bytes;

            // Copy the end of the buffer to the start
            System.arraycopy(search_window, search_window.length - boundary.length, search_window, 0, boundary.length);

            // Refill search_window
            new_bytes = search_window.length - boundary.length;
            new_bytes = (b.remaining() < new_bytes) ? b.remaining() : new_bytes;
            b.get(search_window, boundary.length, new_bytes);
        } while (new_bytes > 0);
        return res;
    }

    @Override
    public CookieHandler getCookies() {
        return this.cookies;
    }

    @Override
    public final Map<String, String> getHeaders() {
        return this.headers;
    }

    @Override
    public final InputStream getInputStream() {
        return this.inputStream;
    }

    @Override
    public final Method getMethod() {
        return this.method;
    }

    /**
     * @deprecated use {@link #getParameters()} instead.
     */
    @Override
    @Deprecated
    public final Map<String, String> getParms() {
        Map<String, String> result = new HashMap<String, String>();
        for (String key : this.parms.keySet()) {
            result.put(key, this.parms.get(key).get(0));
        }

        return result;
    }

    @Override
    public final Map<String, List<String>> getParameters() {
        return this.parms;
    }

    @Override
    public String getQueryParameterString() {
        return this.queryParameterString;
    }

    private RandomAccessFile getTmpBucket() {
        try {
            ITempFile tempFile = this.tempFileManager.createTempFile(null);
            return new RandomAccessFile(tempFile.getName(), "rw");
        } catch (Exception e) {
            throw new Error(e); // we won't recover, so throw an error
        }
    }

    @Override
    public final String getUri() {
        return this.uri;
    }

    /**
     * Deduce body length in bytes. Either from "content-length" header or read
     * bytes.
     */
    public long getBodySize() {
        if (this.headers.containsKey("content-length")) {
            return Long.parseLong(this.headers.get("content-length"));
        }

        return 0;
    }

    @Override
    public void parseBody(Map<String, String> files) throws IOException, ResponseException {

        try {
            bodySize = getBodySize();
            // If the method is POST, there may be parameters
            // in data section, too, read it:
            if (Method.POST.equals(this.method)) {
                ContentType contentType = new ContentType(this.headers.get("content-type"));
                if (contentType.isMultipart()) {
                    String boundary = contentType.getBoundary();
                    if (boundary == null) {
                        throw new ResponseException(Status.BAD_REQUEST, "BAD REQUEST: Content type is multipart/form-data but boundary missing. Usage: GET /example/file.html");
                    }

                    calcOverlap(contentType.getBoundary().getBytes());

                    decodeMultipartFormData(this.inputStream, contentType, this.parms, files);
                } else {
                    String postLine = readRawContent(this.inputStream, bodySize);
                    System.out.println(postLine);
                    // Handle application/x-www-form-urlencoded
                    if ("application/x-www-form-urlencoded".equalsIgnoreCase(contentType.getContentType())) {
                        decodeParms(postLine, this.parms);
                    } else if (postLine.length() != 0) {
                        // Special case for raw POST data => create a
                        // special files entry "postData" with raw content
                        // data
                        files.put(POST_DATA, postLine);
                    }
                }
            } else if (Method.PUT.equals(this.method)) {
                files.put("content", saveTmpFile(this.inputStream, null));
            }
        } finally {
        }
    }

    /**
     * Retrieves the content of a sent file and saves it to a temporary file.
     * The full path to the saved file is returned.
     */

    private String readRawContent(InputStream is, long len) throws IOException {
        if (len <= 0)
            return "";
        StringBuilder builder = new StringBuilder((int) getAvailableDataMemory());
        byte[] buf = new byte[BUFSIZE];
        int read = 0;
        while (len > 0 && read >= 0) {
            read = is.read(buf, 0, (int) Math.min(len, buf.length));
            len -= read;

            builder.append(new String(buf, 0, read));
        }

        return builder.toString();
    }

    private String saveTmpFile(InputStream is, String filename_hint) {
        String path = "";
        FileOutputStream fileOutputStream = null;

        try {
            byte[] buf = new byte[MEMORY_STORE_LIMIT];
            ITempFile tempFile = this.tempFileManager.createTempFile(filename_hint);
            path = tempFile.getName();
            fileOutputStream = new FileOutputStream(tempFile.getName());
            int read = 0;
            while (bodySize > 0 && read >= 0) {
                read = is.read(buf, 0, (int) Math.min(bodySize, buf.length));
                bodySize -= read;
                fileOutputStream.write(buf, 0, read);
            }

        } catch (Exception e) {
            throw new Error(e);
        } finally {
            NanoHTTPD.safeClose(fileOutputStream);
        }

        return path;
    }

    private String saveTmpFile(ByteBuffer b, int offset, int len, String filename_hint) {
        String path = "";
        if (len > 0) {
            FileOutputStream fileOutputStream = null;
            try {
                ITempFile tempFile = this.tempFileManager.createTempFile(filename_hint);
                ByteBuffer src = b.duplicate();
                fileOutputStream = new FileOutputStream(tempFile.getName());
                FileChannel dest = fileOutputStream.getChannel();
                src.position(offset).limit(offset + len);
                dest.write(src.slice());
                path = tempFile.getName();
            } catch (Exception e) { // Catch exception if any
                throw new Error(e); // we won't recover, so throw an error
            } finally {
                NanoHTTPD.safeClose(fileOutputStream);
            }
        }
        return path;
    }

    @Override
    public String getRemoteIpAddress() {
        return this.remoteIp;
    }

    @Override
    public String getRemoteHostName() {
        return this.remoteHostname;
    }
}
