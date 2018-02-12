package com.techshroom.lettar.util;

import java.io.ByteArrayOutputStream;

/**
 * Exposes the internal arrays of {@link ByteArrayOutputStream} for efficiency.
 */
public class ExposedBAOS extends ByteArrayOutputStream {

    public ExposedBAOS() {
    }

    public ExposedBAOS(int size) {
        super(size);
    }

    public byte[] getBuf() {
        return buf;
    }

    public int getCount() {
        return count;
    }

}
