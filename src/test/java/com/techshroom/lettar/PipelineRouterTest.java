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

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.techshroom.lettar.collections.HttpMultimap;
import com.techshroom.lettar.pipe.PipelineRouterInitializer;
import com.techshroom.lettar.routing.HttpMethod;

public class PipelineRouterTest {

    private Router<String, String> router;

    @Before
    public void setup() {
        router = new PipelineRouterInitializer()
                .newRouter(ImmutableList.of(new TestRoutes()));
    }

    @Test
    public void testPathRoutes() throws Exception {
        assertEqualsIgnContentType(SimpleResponse.of(200, "Index Page"), router.route(request("/")));
        assertEqualsIgnContentType(SimpleResponse.of(200, "Index Page"), router.route(request("//")));
        assertEqualsIgnContentType(SimpleResponse.of(200, "[a,b,c,res1]"), router.route(request("/res1/list")));
        assertEqualsIgnContentType(SimpleResponse.of(200, "[a,b,c,res2]"), router.route(request("/res2/list")));
        assertEqualsIgnContentType(SimpleResponse.of(200, "CHOMP! nomnomnom"), router.route(request("/ec/nomnomnom")));
        assertEqualsIgnContentType(SimpleResponse.of(200, "RE: 42"), router.route(request("/re/42")));
        assertEqualsIgnContentType(SimpleResponse.of(404, "404 Page"), router.route(request("/re/NaN")));
    }

    @Test
    public void testBodyTypeDecoder() throws Exception {
        Request<String> request = SimpleRequest.builder("msg")
                .method(HttpMethod.GET)
                .path("/bodytype")
                .build();
        assertEqualsIgnContentType(SimpleResponse.of(200, "msg; type=class java.lang.String"), router.route(request));
    }

    @Test
    public void testQueryRoutes() throws Exception {
        assertEqualsIgnContentType(SimpleResponse.of(200, "Queried 'index' Page"), router.route(request("/query",
                HttpMultimap.copyOf(ImmutableMap.of("page", "index")))));
        assertEqualsIgnContentType(SimpleResponse.of(200, "Queried 'action' Page"), router.route(request("/query",
                HttpMultimap.copyOf(ImmutableMap.of("page", "action", "action", "foobar")))));
    }

    @Test
    public void testErrorRoutes() throws Exception {
        assertEqualsIgnContentType(SimpleResponse.of(500, "Error Here"), router.route(request("/error")));
    }

    @Test
    public void test404Routes() throws Exception {
        SimpleResponse<String> notFound = SimpleResponse.builder("404 Page")
                .statusCode(404)
                .headers(ImmutableMap.of("content-type", "application/octet-stream"))
                .build();

        assertEquals(notFound, router.route(request("/nonexist")).toCompletableFuture().get());
        assertEquals(notFound, router.route(request("//list")).toCompletableFuture().get());
        assertEquals(notFound, router.route(request("/apiary/drapiary/list")).toCompletableFuture().get());
    }

    @Test
    public void test404WithBadContentType() throws Exception {
        SimpleResponse<String> notFound = SimpleResponse.builder("404 Page")
                .statusCode(404)
                .headers(ImmutableMap.of("content-type", "application/octet-stream"))
                .build();

        assertEquals(notFound, router.route(requestBuilder("/json")
                .headers(ImmutableMap.of("accept", "impossible/notathing"))
                .build()).toCompletableFuture().get());
    }

    private static void assertEqualsIgnContentType(SimpleResponse<String> expected, CompletionStage<Response<String>> actualStage) throws Exception {
        Response<String> actual = actualStage.toCompletableFuture().get();
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

    private static Request<String> request(String path) {
        return requestBuilder(path)
                .build();
    }

    private static SimpleRequest.Builder<String> requestBuilder(String path) {
        return SimpleRequest.<String> builder()
                .method(HttpMethod.GET)
                .path(path);
    }

    private static Request<String> request(String path, HttpMultimap query) {
        return requestBuilder(path)
                .queryParts(query)
                .build();
    }

}
