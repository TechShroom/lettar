/*
 * This file is part of lettar, licensed under the MIT License (MIT).
 *
 * Copyright (c) TechShroom Studios <https://techshroom.com>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.techshroom.lettar.addons.sse;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.techshroom.lettar.util.ExposedBAOS;

public class SseInputStream extends InputStream {

    private ExposedBAOS buffer = new ExposedBAOS();
    private int index = 0;
    private final Lock lock = new ReentrantLock();
    private final Condition dataReady = lock.newCondition();
    private int resetAtThisIndex = -1;
    private int eofAtThisIndex = -1;

    public void write(String data) {
        lock.lock();
        try {
            checkOpen();
            buffer.write(data.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            lock.unlock();
        }
    }

    public void write(char data) {
        lock.lock();
        try {
            checkOpen();
            // for UTF-8, single characters are encoded already
            // obviously this doesn't work with surrogate pairs, but that's fine
            buffer.write(data);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int read() throws IOException {
        lock.lock();
        try {
            if (!ensureDataAvailable()) {
                return -1;
            }
            int data = buffer.getBuf()[index];
            index++;
            updateStreamState();
            return data;
        } finally {
            lock.unlock();
        }
    }

    // read(byte[]) delegates to this
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        lock.lock();
        try {
            if (!ensureDataAvailable()) {
                return -1;
            }
            int available = rawAvailable();
            int numRead = Math.min(available, len);
            System.arraycopy(buffer.getBuf(), index, b, off, numRead);
            index += numRead;
            updateStreamState();
            return numRead;
        } finally {
            lock.unlock();
        }
    }

    private boolean ensureDataAvailable() throws IOException {
        while (buffer != null && rawAvailable() == 0) {
            try {
                // wait for a small maximum
                // this ensures that if flush isn't called
                // we still read the data in a reasonable time
                dataReady.await(1, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("thread interrupted", e);
            }
        }
        return buffer != null;
    }

    private void updateStreamState() {
        if (eofAtThisIndex != -1 && index >= eofAtThisIndex) {
            buffer = null;
            return;
        }
        if (resetAtThisIndex != -1 && index >= resetAtThisIndex) {
            int len = rawAvailable();
            buffer.reset();
            buffer.write(buffer.getBuf(), index, len);
            index = 0;
            resetAtThisIndex = -1;
        }
    }

    @Override
    public int available() throws IOException {
        lock.lock();
        try {
            if (buffer == null) {
                return 0;
            }
            return rawAvailable();
        } finally {
            lock.unlock();
        }
    }

    private int rawAvailable() {
        return buffer.getCount() - index;
    }

    public void flush() throws IOException {
        lock.lock();
        try {
            checkOpen();
            resetAtThisIndex = buffer.getCount();
            dataReady.signal();
        } finally {
            lock.unlock();
        }
    }

    private void checkOpen() {
        checkState(rawIsOpen(), "closed");
    }

    private boolean rawIsOpen() {
        return eofAtThisIndex == -1 && buffer != null;
    }
    
    public boolean isOpen() {
        lock.lock();
        try {
            return rawIsOpen();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        lock.lock();
        try {
            eofAtThisIndex = buffer.getCount();
        } finally {
            lock.unlock();
        }
    }

}
