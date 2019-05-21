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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.techshroom.lettar.pipe.PipelineRouterInitializer;
import com.techshroom.lettar.routing.HttpMethod;
import org.junit.Before;
import org.junit.Test;

public class PipelineRouterTest extends AbstractRouterTest {

    private Router<String, String> router;

    @Before
    public void setup() {
        router = new PipelineRouterInitializer()
                .newRouter(ImmutableList.of(new TestRoutes()));
    }

    @Test
    public void testPathRoutes() throws Exception {
        assertRespEqualsIgnContentType(SimpleResponse.of(200, "Index Page"), router.route(request("/")));
        assertRespEqualsIgnContentType(SimpleResponse.of(200, "Index Page"), router.route(request("//")));
        assertRespEqualsIgnContentType(SimpleResponse.of(200, "[a,b,c,res1]"), router.route(request("/res1/list")));
        assertRespEqualsIgnContentType(SimpleResponse.of(200, "[a,b,c,res2]"), router.route(request("/res2/list")));
        assertRespEqualsIgnContentType(SimpleResponse.of(200, "CHOMP! nomnomnom"), router.route(request("/ec/nomnomnom")));
        assertRespEqualsIgnContentType(SimpleResponse.of(200, "RE: 42"), router.route(request("/re/42")));
        assertRespEqualsIgnContentType(SimpleResponse.of(404, "404 Page"), router.route(request("/re/NaN")));
    }

    @Test
    public void testBodyTypeDecoder() throws Exception {
        Request<String> request = SimpleRequest.builder("msg")
                .method(HttpMethod.GET)
                .path("/bodytype")
                .build();
        assertRespEqualsIgnContentType(SimpleResponse.of(200, "msg; type=class java.lang.String"), router.route(request));
    }

    @Test
    public void testQueryRoutes() throws Exception {
        assertRespEqualsIgnContentType(SimpleResponse.of(200, "Queried 'index' Page"), router.route(request("/query",
                ImmutableListMultimap.of("page", "index"))));
        assertRespEqualsIgnContentType(SimpleResponse.of(200, "Queried 'action' Page"), router.route(request("/query",
                ImmutableListMultimap.of("page", "action", "action", "foobar"))));
    }

    @Test
    public void testErrorRoutes() throws Exception {
        assertRespEqualsIgnContentType(SimpleResponse.of(500, "Error Here"), router.route(request("/error")));
    }

    @Test
    public void test404Routes() throws Exception {
        SimpleResponse<String> notFound = SimpleResponse.builder("404 Page")
                .statusCode(404)
                .headers(ImmutableMap.of("content-type", "application/octet-stream"))
                .build();

        assertRespEquals(notFound, router.route(request("/nonexist")));
        assertRespEquals(notFound, router.route(request("//list")));
        assertRespEquals(notFound, router.route(request("/apiary/drapiary/list")));
    }

    @Test
    public void test404WithBadContentType() throws Exception {
        SimpleResponse<String> notFound = SimpleResponse.builder("404 Page")
                .statusCode(404)
                .headers(ImmutableMap.of("content-type", "application/octet-stream"))
                .build();

        assertRespEquals(notFound, router.route(requestBuilder("/json")
                .headers(ImmutableMap.of("accept", "impossible/notathing"))
                .build()));
    }

    @Test
    public void testAsyncRoutes() throws Exception {
        assertRespEqualsIgnContentType(SimpleResponse.of(200, "async op!"), router.route(request("/async")));
    }

}
