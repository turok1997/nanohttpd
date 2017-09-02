package org.nanohttpd.util;

import java.io.IOException;

public interface IPushbackByteReader {


    int getByte() throws IOException;
    void getBytes(byte[] buf, int offset, int length) throws IOException;
    void ungetLastByte() throws IOException;
    void pushByteBack(int b) throws IOException;
    void ungetBytes(byte[] buf) throws IOException;
}
