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

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.techshroom.lettar.util.HttpUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * HTTP case-insensitive multiple value map.
 */
@AutoValue
public abstract class HttpMultimap {

    public static HttpMultimap of() {
        return wrap(ImmutableSortedMap.of());
    }

    public static HttpMultimap copyOfSingle(Map<String, String> map) {
        ImmutableSortedMap.Builder<String, ImmutableList<String>> copy = HttpUtil.headerMapBuilder();
        for (Map.Entry<String, String> header : map.entrySet()) {
            copy.put(header.getKey(), ImmutableList.of(header.getValue()));
        }
        return wrap(copy.build());
    }

    public static HttpMultimap copyOf(ListMultimap<String, String> map) {
        return copyOf(Multimaps.asMap(map));
    }

    public static HttpMultimap copyOf(Map<String, ? extends List<String>> map) {
        if (map instanceof ImmutableSortedMap) {
            @SuppressWarnings("unchecked")
            ImmutableSortedMap<String, ? extends List<String>> ism = (ImmutableSortedMap<String, ? extends List<String>>) map;
            if (ism.comparator() == String.CASE_INSENSITIVE_ORDER
                && ism.values().stream().allMatch(v -> v instanceof ImmutableList)) {
                // already validated for immutable. pass to wrap
                @SuppressWarnings("unchecked")
                ImmutableSortedMap<String, ImmutableList<String>> ismIl = (ImmutableSortedMap<String, ImmutableList<String>>) ism;
                return wrap(ismIl);
            }
        }
        ImmutableSortedMap.Builder<String, ImmutableList<String>> copy = HttpUtil.headerMapBuilder();
        for (Map.Entry<String, ? extends List<String>> header : map.entrySet()) {
            if (header.getValue().size() > 0) {
                copy.put(header.getKey(), ImmutableList.copyOf(header.getValue()));
            }
        }
        return wrap(copy.build());
    }

    private static HttpMultimap wrap(ImmutableSortedMap<String, ImmutableList<String>> map) {
        return new AutoValue_HttpMultimap(map);
    }

    HttpMultimap() {
    }

    abstract ImmutableSortedMap<String, ImmutableList<String>> getMultimap();

    public Optional<String> getSingleValue(String key) {
        String value = getSingleValueOrDefault(key, null);
        return Optional.ofNullable(value);
    }

    public String getSingleValueOrDefault(String key, String defaultValue) {
        return Iterables.getFirst(getMultimap().getOrDefault(key, ImmutableList.of()), defaultValue);
    }

    public HttpMultimap transform(UnaryOperator<ImmutableSortedMap<String, ImmutableList<String>>> change) {
        ImmutableSortedMap<String, ImmutableList<String>> newList = change.apply(getMultimap());
        if (getMultimap().equals(newList)) {
            return this;
        }
        return copyOf(newList);
    }

    private HttpMultimap changeKey(String key, UnaryOperator<ImmutableList<String>> change) {
        ImmutableSortedMap.Builder<String, ImmutableList<String>> headers = HttpUtil.headerMapBuilder();
        getMultimap().forEach((k, v) -> {
            if (!k.equalsIgnoreCase(key)) {
                headers.put(k, v);
            }
        });
        ImmutableList<String> newValue = change.apply(getMultimap().get(key));
        if (newValue != null) {
            headers.put(key, newValue);
        }
        return wrap(headers.build());
    }

    public HttpMultimap add(String key, String value) {
        return changeKey(key, v -> v == null
            ? ImmutableList.of(value)
            : ImmutableList.<String>builder()
            .addAll(v)
            .add(value)
            .build());
    }

    public HttpMultimap set(String key, String value) {
        return changeKey(key, v -> ImmutableList.of(value));
    }

    public HttpMultimap setIfAbsent(String key, String value) {
        if (getMultimap().containsKey(key)) {
            return this;
        }
        return set(key, value);
    }

    public HttpMultimap remove(String key) {
        return changeKey(key, v -> null);
    }

    public ImmutableSet<Map.Entry<String, ImmutableList<String>>> entrySet() {
        return getMultimap().entrySet();
    }

}
