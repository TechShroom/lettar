package com.techshroom.lettar;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
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
            try {
                emitter.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
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
