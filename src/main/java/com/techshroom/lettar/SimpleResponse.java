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

import com.google.auto.value.AutoValue;
import com.google.common.collect.ListMultimap;
import com.techshroom.lettar.collections.HttpMultimap;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.UnaryOperator;

@AutoValue
public abstract class SimpleResponse<B> implements Response<B> {

    public static <B> SimpleResponse<B> of(int code, @Nullable B body) {
        return SimpleResponse.<B> builder().body(body).statusCode(code).build();
    }

    public static <B> Builder<B> builder() {
        return new AutoValue_SimpleResponse.Builder<B>()
                .headers(HttpMultimap.of());
    }

    public static <B> Builder<B> builder(@Nullable B body) {
        return SimpleResponse.<B> builder().body(body);
    }

    @AutoValue.Builder
    public interface Builder<B> {

        Builder<B> headers(HttpMultimap headers);

        default Builder<B> headers(Map<String, String> headers) {
            return headers(HttpMultimap.copyOfSingle(headers));
        }

        default Builder<B> headers(ListMultimap<String, String> headers) {
            return headers(HttpMultimap.copyOf(headers));
        }

        HttpMultimap getHeaders();

        default Builder<B> modifyHeaders(UnaryOperator<HttpMultimap> mod) {
            return headers(mod.apply(getHeaders()));
        }

        Builder<B> body(@Nullable B body);

        Builder<B> statusCode(int code);

        default Builder<B> ok_200() {
            return statusCode(200);
        }

        default Builder<B> noContent_201() {
            return statusCode(201);
        }

        OptionalInt getStatusCode();

        default Builder<B> statusCodeIfUnset(int code) {
            return statusCode(getStatusCode().orElse(code));
        }

        SimpleResponse<B> build();

    }

    SimpleResponse() {
    }

    public abstract Builder<B> toBuilder();

}
