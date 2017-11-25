package com.techshroom.lettar;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

/**
 * A response is a reply to a routed {@link Request}. It contains a status code,
 * body, and headers.
 */
public interface Response<B> {

    int getStatusCode();

    @Nullable
    B getBody();

    ImmutableMap<String, String> getHeaders();

}
