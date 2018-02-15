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

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import com.google.common.base.Splitter;
import com.techshroom.lettar.Response;
import com.techshroom.lettar.SimpleResponse;
import com.techshroom.lettar.addons.sse.SseInputStream.SseOutput;

public class BaseSseEmitter implements SseEmitter {

    private static final Splitter LINE_SPLITTER = Splitter.onPattern("\r?\n");

    private final CompletableFuture<Response<InputStream>> stage;
    private final SseInputStream stream = new SseInputStream();
    private final SseOutput output = stream.getOutput();

    public BaseSseEmitter(Supplier<SimpleResponse.Builder<InputStream>> responseBuilder) {
        SimpleResponse<InputStream> response = responseBuilder.get()
                .statusCodeIfUnset(200)
                .body(stream)
                .modifyHeaders(headers -> headers
                        .setIfAbsent("content-type", "text/event-stream")
                        .setIfAbsent("transfer-encoding", "chunked"))
                .build();
        stage = CompletableFuture.completedFuture(response);
    }

    @Override
    public boolean emit(ServerSentEvent event) {
        // lock so that no exceptions are produced
        // because the stream can't close while we're writing
        output.lock();
        try {
            if (!stream.isOpen()) {
                return false;
            }
            if (event.getComment().isPresent()) {
                for (String commentLine : LINE_SPLITTER.split(event.getComment().get())) {
                    output.write(": ");
                    output.write(commentLine);
                    output.write('\n');
                }
            }
            if (event.getName().isPresent()) {
                output.write("event: ");
                output.write(event.getName().get());
                output.write('\n');
            }
            if (event.getId().isPresent()) {
                output.write("id: ");
                output.write(event.getId().get());
                output.write('\n');
            }
            if (event.getData().isPresent()) {
                for (String dataLine : LINE_SPLITTER.split(event.getData().get())) {
                    output.write("data: ");
                    output.write(dataLine);
                    output.write('\n');
                }
            }
            // ends the event
            output.write('\n');

            output.flush();
        } finally {
            output.unlock();
        }
        return true;
    }

    @Override
    public CompletionStage<Response<InputStream>> getResponseStage() {
        return stage;
    }

    @Override
    public void close() {
        output.close();
    }

    @Override
    public boolean isOpen() {
        return stream.isOpen();
    }

}
