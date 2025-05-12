package com.example.quizcards.utils;

import java.io.IOException;
import java.io.InputStream;

public class LimitedInputStream extends InputStream {
    private final InputStream in;
    private long left;
    private long skipped;

    public LimitedInputStream(InputStream in, long start, long length) throws IOException {
        this.in = in;
        this.left = length;
        this.skipped = in.skip(start);
    }

    @Override
    public int read() throws IOException {
        if (left <= 0) return -1;
        int result = in.read();
        if (result != -1) {
            left--;
        }
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (left <= 0) return -1;
        len = (int) Math.min(len, left);
        int result = in.read(b, off, len);
        if (result != -1) {
            left -= result;
        }
        return result;
    }
}
