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

import java.util.Map;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.techshroom.lettar.routing.HttpMethod;

@AutoValue
public abstract class SimpleRequest<B> implements Request<B> {

    public static <B> Builder<B> builder() {
        return new AutoValue_SimpleRequest.Builder<B>()
                .headers(ImmutableMap.of())
                .queryParts(ImmutableMultimap.of());
    }

    @AutoValue.Builder
    public interface Builder<B> {

        Builder<B> method(HttpMethod method);

        Builder<B> path(String path);

        Builder<B> headers(ImmutableMap<String, String> headers);

        default Builder<B> headers(Map<String, String> headers) {
            return headers(HttpUtil.headerMapBuilder().putAll(headers).build());
        }

        Builder<B> queryParts(Multimap<String, String> headers);

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
