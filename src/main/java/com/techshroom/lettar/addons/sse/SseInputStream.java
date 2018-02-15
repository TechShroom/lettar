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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SseInputStream extends InputStream {

    public static class SseOutput {

        private final SseInputStream stream;
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        private SseOutput(SseInputStream stream) {
            this.stream = stream;
        }

        public void write(String data) {
            stream.lock.lock();
            try {
                checkOpen();
                byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
                buffer.write(bytes, 0, bytes.length);
            } finally {
                stream.lock.unlock();
            }
        }

        public void write(char data) {
            stream.lock.lock();
            try {
                checkOpen();
                // for UTF-8, single characters are encoded already
                // obviously this doesn't work with surrogate pairs
                // but that's fine
                buffer.write(data);
            } finally {
                stream.lock.unlock();
            }
        }

        public void flush() {
            try {
                stream.packets.put(buffer.toByteArray());
                buffer.reset();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                // we shouldn't ever block here -- queue is infinite
                throw new IllegalStateException(e);
            }
        }

        private void checkOpen() {
            checkState(!stream.closed, "closed");
        }

        public void close() {
            stream.lock.lock();
            try {
                flush();
                stream.close();
            } finally {
                stream.lock.unlock();
            }
        }

        public void lock() {
            stream.lock.lock();
        }

        public void unlock() {
            stream.lock.unlock();
        }

    }

    private final BlockingQueue<byte[]> packets = new LinkedBlockingQueue<>();
    private byte[] current;
    private int index;
    private final SseOutput output = new SseOutput(this);
    private final Lock lock = new ReentrantLock();
    private boolean closed;

    public SseOutput getOutput() {
        return output;
    }

    @Override
    public int read() throws IOException {
        byte[] next = ensureDataAvailable(true);
        if (next == null) {
            return -1;
        }
        int data = next[index];
        index++;
        return data;
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

        byte[] next = ensureDataAvailable(true);
        if (next == null) {
            return -1;
        }
        int numRead = Math.min(next.length - index, len);
        System.arraycopy(next, index, b, off, numRead);
        index += numRead;
        return numRead;
    }

    private byte[] ensureDataAvailable(boolean wait) throws IOException {
        if (current != null && index >= current.length) {
            current = null;
            index = 0;
        }
        while (current == null && morePacketsComing()) {
            if (wait) {
                try {
                    current = packets.poll(1, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            } else {
                current = packets.poll();
            }
        }
        return current;
    }

    private boolean morePacketsComing() {
        lock.lock();
        try {
            return !closed || packets.size() > 0;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int available() throws IOException {
        byte[] data = ensureDataAvailable(false);
        if (data == null) {
            // none immediately available
            return 0;
        }
        return data.length - index;
    }

    public boolean isOpen() {
        lock.lock();
        try {
            return !closed;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        lock.lock();
        try {
            closed = true;
        } finally {
            lock.unlock();
        }
    }

}
