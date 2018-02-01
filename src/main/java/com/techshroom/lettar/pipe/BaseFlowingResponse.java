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

import com.google.common.collect.ImmutableMap;
import com.techshroom.lettar.collections.HttpMultimap;

class BaseFlowingResponse extends BaseFlowingElement<FlowingResponse> implements FlowingResponse {

    public static FlowingResponse from(int status, Object body, HttpMultimap headers) {
        return new BFEBuilder<>(ImmutableMap.of(), BaseFlowingResponse::new)
                .put(ResponseKeys.statusCode, status)
                .put(ResponseKeys.body(), body)
                .put(ResponseKeys.headers, headers)
                .build();
    }

    public BaseFlowingResponse(ImmutableMap<String, Optional<Object>> map) {
        super(map);
    }

    @Override
    protected Function<ImmutableMap<String, Optional<Object>>, FlowingResponse> constructFunction() {
        return BaseFlowingResponse::new;
    }

}
