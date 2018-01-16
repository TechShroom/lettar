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
package com.techshroom.lettar.collections;

import java.util.Map;
import java.util.Optional;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.techshroom.lettar.HttpUtil;

@AutoValue
public abstract class HttpMultimap {

    public static HttpMultimap of() {
        // if it's empty, it's always sorted :)
        return copyOfPreSorted(ImmutableListMultimap.of());
    }

    public static HttpMultimap copyOf(Map<String, String> map) {
        return copyOfPreSorted(HttpUtil.headerMapBuilder().putAll(map.entrySet()).build());
    }

    public static HttpMultimap copyOf(Multimap<String, String> map) {
        if (map instanceof ImmutableListMultimap) {
            // assume sorted properly
            return copyOfPreSorted((ImmutableListMultimap<String, String>) map);
        }
        return copyOfPreSorted(HttpUtil.headerMapBuilder().putAll(map).build());
    }

    public static HttpMultimap copyOfPreSorted(ImmutableListMultimap<String, String> map) {
        return new AutoValue_HttpMultimap(map);
    }

    HttpMultimap() {
    }

    public abstract ImmutableListMultimap<String, String> getMultimap();

    public Optional<String> getSingleValue(String key) {
        String value = getSingleValueOrDefault(key, null);
        return Optional.ofNullable(value);
    }

    public String getSingleValueOrDefault(String key, String defaultValue) {
        return Iterables.getFirst(getMultimap().get(key), defaultValue);
    }

}
