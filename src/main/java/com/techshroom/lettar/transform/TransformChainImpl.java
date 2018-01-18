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
package com.techshroom.lettar.transform;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.techshroom.lettar.Response;
import com.techshroom.lettar.routing.Request;

public class TransformChainImpl<I, O> implements TransformChain<I, O> {

    public static <I, O> TransformChainImpl<I, O> create(Collection<RouteTransform<?, ?, ?>> transforms) {
        return new TransformChainImpl<>(ImmutableList.copyOf(transforms));
    }

    private final List<RouteTransform<?, ?, ?>> transforms;
    private int index = -1;
    private Request<?> request;

    private TransformChainImpl(List<RouteTransform<?, ?, ?>> transforms) {
        this.transforms = transforms;
    }

    @Override
    public Request<I> request() {
        // assume safe - no way to verify
        @SuppressWarnings("unchecked")
        Request<I> req = (Request<I>) request;
        return req;
    }

    @Override
    public <NI> TransformChain<NI, O> withRequest(Request<NI> request) {
        this.request = request;
        @SuppressWarnings("unchecked")
        TransformChain<NI, O> thisCast = (TransformChain<NI, O>) this;
        return thisCast;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Response<O> next() {
        TransformChainImpl<Object, Object> chain = (TransformChainImpl<Object, Object>) this;
        index++;
        try {
            RouteTransform<Object, Object, O> nextTransform = nextTransform();
            Response<O> result = nextTransform.transform(chain);
            return result;
        } finally {
            index--;
        }
    }

    @SuppressWarnings("unchecked")
    private RouteTransform<Object, Object, O> nextTransform() {
        if (index >= transforms.size()) {
            throw new IllegalStateException("Index violation, transforms=" + transforms);
        }
        return (RouteTransform<Object, Object, O>) transforms.get(index);
    }

}
