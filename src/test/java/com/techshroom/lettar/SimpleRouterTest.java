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

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.techshroom.lettar.routing.HttpMethod;

public class SimpleRouterTest {

    private SimpleRouter<String, String> router;

    @Before
    public void setup() {
        router = SimpleRouter.create();
    }

    private void registerRoutes() {
        router.registerRoutes(ImmutableList.of(new TestRoutes()));
    }

    @Test
    public void testRegisterRoutes() throws Exception {
        registerRoutes();
    }

    @Test
    public void testPathRoutes() throws Exception {
        registerRoutes();

        assertEqualsIgnContentType(SimpleResponse.of(200, "Index Page"), router.route(request("/")));
        assertEqualsIgnContentType(SimpleResponse.of(200, "Index Page"), router.route(request("//")));
        assertEqualsIgnContentType(SimpleResponse.of(200, "[a,b,c,res1]"), router.route(request("/res1/list")));
        assertEqualsIgnContentType(SimpleResponse.of(200, "[a,b,c,res2]"), router.route(request("/res2/list")));
        assertEqualsIgnContentType(SimpleResponse.of(200, "CHOMP! nomnomnom"), router.route(request("/ec/nomnomnom")));
        assertEqualsIgnContentType(SimpleResponse.of(200, "RE: 42"), router.route(request("/re/42")));
        assertEqualsIgnContentType(SimpleResponse.of(404, "404 Page"), router.route(request("/re/NaN")));
    }

    @Test
    public void testQueryRoutes() throws Exception {
        registerRoutes();

        assertEqualsIgnContentType(SimpleResponse.of(200, "Queried 'index' Page"), router.route(request("/query",
                ImmutableMultimap.of("page", "index"))));
        assertEqualsIgnContentType(SimpleResponse.of(200, "Queried 'action' Page"), router.route(request("/query",
                ImmutableMultimap.of("page", "action", "action", "foobar"))));
    }

    @Test
    public void testErrorRoutes() throws Exception {
        registerRoutes();

        assertEqualsIgnContentType(SimpleResponse.of(500, "Error encountered: Error Here"), router.route(request("/error")));
    }

    @Test
    public void test404Routes() throws Exception {
        registerRoutes();

        SimpleResponse<String> notFound = SimpleResponse.of(404, "404 Page");

        assertEquals(notFound, router.route(request("/nonexist")));
        assertEquals(notFound, router.route(request("//list")));
        assertEquals(notFound, router.route(request("/apiary/drapiary/list")));
    }

    private static void assertEqualsIgnContentType(SimpleResponse<String> expected, Response<String> actual) {
        assertEquals(expected.getBody(), actual.getBody());
        assertEquals(expected.getStatusCode(), actual.getStatusCode());
        Map<String, String> headers = HttpUtil.headerMapBuilder().putAll(
                actual.getHeaders().entrySet().stream()
                        .filter(e -> !"content-type".equalsIgnoreCase(e.getKey()))
                        .collect(toImmutableList()))
                .build();
        assertEquals(expected.getHeaders(), headers);
    }

    private static Request<String> request(String path) {
        return SimpleRequest.<String> builder()
                .method(HttpMethod.GET)
                .path(path)
                .build();
    }

    private static Request<String> request(String path, Multimap<String, String> query) {
        return SimpleRequest.<String> builder()
                .method(HttpMethod.GET)
                .path(path)
                .queryParts(query)
                .build();
    }

}
