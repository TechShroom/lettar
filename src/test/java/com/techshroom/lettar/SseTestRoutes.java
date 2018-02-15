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
package com.techshroom.lettar;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;

import com.techshroom.lettar.addons.sse.BaseSseEmitter;
import com.techshroom.lettar.addons.sse.ServerSentEvent;
import com.techshroom.lettar.addons.sse.SseEmitter;
import com.techshroom.lettar.annotation.NotFoundHandler;
import com.techshroom.lettar.annotation.ServerErrorHandler;
import com.techshroom.lettar.pipe.builtins.path.Path;

public class SseTestRoutes {

    @Path("/sse")
    public CompletionStage<Response<InputStream>> sse() {
        SseEmitter emitter = new BaseSseEmitter(SimpleResponse::builder);
        Thread t = new Thread(() -> {
            emitter.emit(ServerSentEvent.of("test", "1", "event 1"));
            emitter.emit(ServerSentEvent.of("test", "2", "event 2"));
            emitter.close();
        }, "emitter");
        t.start();
        return emitter.getResponseStage();
    }

    @NotFoundHandler
    public Response<InputStream> notFound() {
        return SimpleResponse.of(404, new ByteArrayInputStream("404".getBytes(StandardCharsets.UTF_8)));
    }

    @ServerErrorHandler
    public Response<InputStream> error(Throwable t) {
        t.printStackTrace();
        return SimpleResponse.of(500, new ByteArrayInputStream("500".getBytes(StandardCharsets.UTF_8)));
    }

}
