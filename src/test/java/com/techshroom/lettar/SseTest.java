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

import static org.junit.Assert.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.techshroom.lettar.pipe.PipelineRouterInitializer;

public class SseTest extends AbstractRouterTest {

    private Router<String, InputStream> router;

    @Before
    public void setup() {
        router = new PipelineRouterInitializer()
                .newRouter(ImmutableList.of(new SseTestRoutes()));
    }

    @Test
    public void testSse() throws Exception {
        Response<InputStream> response = getStageValue(router.route(request("/sse")));
        byte[] data = ByteStreams.toByteArray(response.getBody());
        byte[] expected = ("event: test\n" +
                "id: 1\n" +
                "data: event 1\n" +
                "\n" +
                "event: test\n" +
                "id: 2\n" +
                "data: event 2\n" +
                "\n").getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, data);
    }

}
