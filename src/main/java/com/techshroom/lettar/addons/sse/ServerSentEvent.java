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
package com.techshroom.lettar.addons.sse;

import java.util.Optional;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ServerSentEvent {

    public static ServerSentEvent of(@Nullable String name, @Nullable String id, @Nullable String data) {
        return builder().name(name).id(id).data(data).build();
    }

    public static Builder builder() {
        return new AutoValue_ServerSentEvent.Builder();
    }

    @AutoValue.Builder
    public interface Builder {

        Builder comment(@Nullable String comment);

        Builder name(@Nullable String name);

        Builder id(@Nullable String id);

        Builder data(@Nullable String data);

        ServerSentEvent build();

    }

    ServerSentEvent() {
    }

    public abstract Optional<String> getComment();

    public abstract Optional<String> getName();

    public abstract Optional<String> getId();

    public abstract Optional<String> getData();

}
