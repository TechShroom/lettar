package com.techshroom.lettar.addons.sse;

import java.util.Optional;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ServerSentEvent {

    public static ServerSentEvent of(@Nullable String name, @Nullable String id, @Nullable String data) {
        return builder().name(name).id(id).data(data).build();
    }

    public static Builder builder() {
        return new AutoValue_ServerSentEvent.Builder();
    }

    @AutoValue.Builder
    public interface Builder {

        Builder comment(@Nullable String comment);

        Builder name(@Nullable String name);

        Builder id(@Nullable String id);

        Builder data(@Nullable String data);

        ServerSentEvent build();

    }

    ServerSentEvent() {
    }

    public abstract Optional<String> getComment();

    public abstract Optional<String> getName();

    public abstract Optional<String> getId();

    public abstract Optional<String> getData();

}
