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
