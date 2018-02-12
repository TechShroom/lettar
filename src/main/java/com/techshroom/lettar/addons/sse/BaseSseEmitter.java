package com.techshroom.lettar.addons.sse;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import com.google.common.base.Splitter;
import com.techshroom.lettar.Response;
import com.techshroom.lettar.SimpleResponse;

public class BaseSseEmitter implements SseEmitter {

    private static final Splitter LINE_SPLITTER = Splitter.onPattern("\r?\n");

    private final CompletableFuture<Response<InputStream>> stage;
    private final SseInputStream stream = new SseInputStream();

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
    public void emit(ServerSentEvent event) {
        try {
            if (event.getComment().isPresent()) {
                for (String commentLine : LINE_SPLITTER.split(event.getComment().get())) {
                    stream.write(": ");
                    stream.write(commentLine);
                    stream.write('\n');
                }
            }
            if (event.getName().isPresent()) {
                stream.write("event: ");
                stream.write(event.getName().get());
                stream.write('\n');
            }
            if (event.getId().isPresent()) {
                stream.write("id: ");
                stream.write(event.getId().get());
                stream.write('\n');
            }
            if (event.getData().isPresent()) {
                for (String dataLine : LINE_SPLITTER.split(event.getData().get())) {
                    stream.write("data: ");
                    stream.write(dataLine);
                    stream.write('\n');
                }
            }
            // ends the event
            stream.write('\n');

            stream.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public CompletionStage<Response<InputStream>> getResponseStage() {
        return stage;
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    @Override
    public boolean isOpen() {
        return stream.isOpen();
    }

}
