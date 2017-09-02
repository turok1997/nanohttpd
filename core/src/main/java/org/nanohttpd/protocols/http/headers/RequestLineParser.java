package org.nanohttpd.protocols.http.headers;

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
        if ( requestLine == null )
            throw new EmptyHeaderException("BAD REQUEST. No request line. Seems the end of stream is reached");

        this.lineTokenizer = new StringTokenizer(requestLine);

        String methodInRequest = getMethod();

        String uri = getUri();

        String version = getHttpVersion();
        if ( methodInRequest == null )
            throw new BadHeaderException(null, "BAD REQUEST: Missing request line (http method)");

        HTTPRequestLine httpRequestLine = new HTTPRequestLine(methodInRequest, uri, version);
        return httpRequestLine;
    }

    private String getMethod(){
        if ( lineTokenizer.hasMoreTokens() )
            return lineTokenizer.nextToken();
        return null;
    }

    private String getUri(){
        if ( lineTokenizer.hasMoreTokens() )
            return lineTokenizer.nextToken();
        return null;
    }

    private String getHttpVersion(){
        if ( lineTokenizer.hasMoreTokens() )
            return lineTokenizer.nextToken();
        return null;
    }

    private String readLine() throws IOException {
        int read = -1;
        StringBuilder sb = new StringBuilder(10);

        while ( (read = requestReader.read()) != -1 && read != '\n' )
        {
            sb.append((char)read);
        }
        if ( read == -1 && sb.length() == 0 )
            return null;
        if ( sb.length() > 0 && sb.charAt(sb.length()-1) == '\r' )
            sb.deleteCharAt(sb.length()-1);

        return sb.toString();
    }

}
