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
package com.techshroom.lettar.pipe.builtins.method;

import com.techshroom.lettar.pipe.BiPipe;
import com.techshroom.lettar.pipe.FilterPipe;
import com.techshroom.lettar.pipe.FlowingRequest;
import com.techshroom.lettar.pipe.FlowingResponse;
import com.techshroom.lettar.pipe.Key;
import com.techshroom.lettar.pipe.ResponseKeys;
import com.techshroom.lettar.routing.HttpMethod;
import com.techshroom.lettar.routing.HttpMethodPredicate;

public class MethodPipe implements FilterPipe, BiPipe {

    public static MethodPipe create(HttpMethodPredicate methodMatcher) {
        return new MethodPipe(methodMatcher);
    }

    private static final Key<Boolean> HEAD_AS_GET = Key.of("head-as-get");

    private final HttpMethodPredicate methodMatcher;

    private MethodPipe(HttpMethodPredicate methodMatcher) {
        this.methodMatcher = methodMatcher;
    }

    @Override
    public boolean accepts(FlowingRequest request) {
        if (methodMatcher.matches(request.getMethod())) {
            return true;
        }

        if (request.getMethod() == HttpMethod.HEAD) {
            // Simulate with GET if needed
            return methodMatcher.matches(HttpMethod.GET);
        }
        return false;
    }

    @Override
    public FlowingRequest pipeIn(FlowingRequest request) {
        // Check if we need to set the "HEAD-as-GET" flag
        if (request.getMethod() == HttpMethod.HEAD && methodMatcher.matches(HttpMethod.GET)) {
            return request.with(HEAD_AS_GET, true);
        }
        return request;
    }

    @Override
    public FlowingResponse pipeOut(FlowingResponse response) {
        Boolean headAsGet = response.getRequest().get(HEAD_AS_GET);
        if (headAsGet != null && headAsGet) {
            // drop the body
            return response.with(ResponseKeys.body(), null);
        }
        return response;
    }

    @Override
    public String toString() {
        return "Method{" + methodMatcher + "}";
    }
}
