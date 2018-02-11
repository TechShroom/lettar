package com.techshroom.lettar;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Multimap;
import com.techshroom.lettar.collections.HttpMultimap;
import com.techshroom.lettar.routing.HttpMethod;
import com.techshroom.lettar.util.HttpUtil;

public abstract class AbstractRouterTest {

    protected static <T> void assertRespEquals(SimpleResponse<T> expected, CompletionStage<Response<T>> actualStage) throws Exception {
        Response<T> actual = getStageValue(actualStage);
        assertEquals(expected.getBody(), actual.getBody());
        assertEquals(expected.getStatusCode(), actual.getStatusCode());
        assertEquals(expected.getHeaders(), actual.getHeaders());
    }

    protected static <T> void assertRespEqualsIgnContentType(SimpleResponse<T> expected, CompletionStage<Response<T>> actualStage) throws Exception {
        Response<T> actual = getStageValue(actualStage);
        assertEquals(expected.getBody(), actual.getBody());
        assertEquals(expected.getStatusCode(), actual.getStatusCode());
        // strip content type from headers
        Multimap<String, String> headers = HttpUtil.headerMapBuilder().putAll(
                actual.getHeaders().getMultimap().entries().stream()
                        .filter(e -> !"content-type".equalsIgnoreCase(e.getKey()))
                        .collect(toImmutableList()))
                .build();
        assertEquals(expected.getHeaders().getMultimap(), headers);
    }

    protected static Request<String> request(String path) {
        return requestBuilder(path)
                .build();
    }

    protected static SimpleRequest.Builder<String> requestBuilder(String path) {
        return SimpleRequest.<String> builder()
                .method(HttpMethod.GET)
                .path(path);
    }

    protected static Request<String> request(String path, HttpMultimap query) {
        return requestBuilder(path)
                .queryParts(query)
                .build();
    }

    protected static <T> T getStageValue(CompletionStage<T> stage) throws Exception {
        return stage.toCompletableFuture().get(10, TimeUnit.SECONDS);
    }
}
