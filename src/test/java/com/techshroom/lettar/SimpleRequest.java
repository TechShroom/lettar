package com.techshroom.lettar;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.techshroom.lettar.routing.HttpMethod;

@AutoValue
public abstract class SimpleRequest<B> implements Request<B> {

    public static <B> Builder<B> builder() {
        return new AutoValue_SimpleRequest.Builder<B>()
                .headers(ImmutableMap.of())
                .queryParts(ImmutableMap.of());
    }

    @AutoValue.Builder
    public interface Builder<B> {
        
        Builder<B> method(HttpMethod method);
        
        Builder<B> path(String path);

        Builder<B> headers(ImmutableMap<String, String> headers);

        default Builder<B> headers(Map<String, String> headers) {
            return headers(HttpUtil.headerMapBuilder().putAll(headers).build());
        }

        Builder<B> queryParts(Map<String, String> headers);

        Builder<B> body(@Nullable B body);

        SimpleRequest<B> build();

    }

    SimpleRequest() {
    }

}
