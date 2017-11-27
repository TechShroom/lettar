package com.techshroom.lettar;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;

@AutoValue
public abstract class SimpleResponse<B> implements Response<B> {

    public static <B> SimpleResponse<B> of(int code, @Nullable B body) {
        return SimpleResponse.<B> builder().body(body).statusCode(code).build();
    }

    public static <B> Builder<B> builder() {
        return new AutoValue_SimpleResponse.Builder<B>()
                .headers(ImmutableMap.of());
    }

    @AutoValue.Builder
    public interface Builder<B> {

        Builder<B> headers(ImmutableMap<String, String> headers);

        default Builder<B> headers(Map<String, String> headers) {
            return headers(HttpUtil.headerMapBuilder().putAll(headers).build());
        }

        Builder<B> body(@Nullable B body);

        Builder<B> statusCode(int code);

        default Builder<B> ok_200() {
            return statusCode(200);
        }

        default Builder<B> noContent_201() {
            return statusCode(201);
        }

        SimpleResponse<B> build();

    }

    SimpleResponse() {
    }

}
