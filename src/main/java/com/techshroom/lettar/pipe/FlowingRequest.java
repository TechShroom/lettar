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

import java.util.List;

import com.google.common.collect.ImmutableListMultimap;
import com.techshroom.lettar.collections.HttpMultimap;
import com.techshroom.lettar.routing.HttpMethod;

public interface FlowingRequest extends FlowingElement<FlowingRequest> {

    default HttpMethod getMethod() {
        return get(RequestKeys.method);
    }

    default List<String> getPath() {
        return get(RequestKeys.path);
    }

    default ImmutableListMultimap<String, String> getQueryParts() {
        return get(RequestKeys.queryParts);
    }

    default HttpMultimap getHeaders() {
        return get(RequestKeys.headers);
    }

    default <B> B getBody() {
        return get(RequestKeys.body());
    }

}
