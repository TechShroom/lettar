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
package com.techshroom.lettar.pipe;

import java.util.Optional;
import java.util.function.Function;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.techshroom.lettar.routing.Request;

class BaseFlowingRequest extends BaseFlowingElement<FlowingRequest> implements FlowingRequest {

    private static final Splitter PATH_SPLITTER = Splitter.on('/').omitEmptyStrings();

    public static FlowingRequest wrap(Request<?> request) {
        return new BFEBuilder<>(ImmutableMap.of(), BaseFlowingRequest::new)
                .put(RequestKeys.method, request.getMethod())
                .put(RequestKeys.path, PATH_SPLITTER.splitToList(request.getPath()))
                .put(RequestKeys.queryParts, request.getQueryParts())
                .put(RequestKeys.headers, request.getHeaders())
                .put(RequestKeys.body(), request.getBody())
                .build();
    }

    public BaseFlowingRequest(ImmutableMap<String, Optional<Object>> map) {
        super(map);
    }

    @Override
    protected Function<ImmutableMap<String, Optional<Object>>, FlowingRequest> constructFunction() {
        return BaseFlowingRequest::new;
    }

}
