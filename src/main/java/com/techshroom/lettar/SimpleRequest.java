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

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.techshroom.lettar.collections.HttpMultimap;
import com.techshroom.lettar.routing.HttpMethod;
import com.techshroom.lettar.routing.Request;

@AutoValue
public abstract class SimpleRequest<B> implements Request<B> {

    public static <B> SimpleRequest<B> copyOfWithBody(Request<?> request, B body) {
        return SimpleRequest.<B> builder()
                .method(request.getMethod())
                .path(request.getPath())
                .queryParts(request.getQueryParts())
                .headers(request.getHeaders())
                .body(body)
                .build();
    }

    public static <B> Builder<B> builder() {
        return new AutoValue_SimpleRequest.Builder<B>()
                .headers(HttpMultimap.of())
                .queryParts(HttpMultimap.of());
    }

    @AutoValue.Builder
    public interface Builder<B> {

        Builder<B> method(HttpMethod method);

        Builder<B> path(String path);

        Builder<B> headers(HttpMultimap headers);

        Builder<B> queryParts(HttpMultimap queryParts);

        Builder<B> body(@Nullable B body);

        SimpleRequest<B> build();

    }

    SimpleRequest() {
    }

    @Override
    public <U> Request<U> withBody(U body) {
        @SuppressWarnings("unchecked")
        Builder<U> builder = (Builder<U>) toBuilder();
        return builder.body(body).build();
    }

    public abstract Builder<B> toBuilder();

}
