package com.techshroom.lettar.addons.sse;

import java.io.Closeable;
import java.io.InputStream;
import java.util.concurrent.CompletionStage;

import com.techshroom.lettar.Response;

public interface SseEmitter extends Closeable {

    void emit(ServerSentEvent event);

    CompletionStage<Response<InputStream>> getResponseStage();

    boolean isOpen();

}
