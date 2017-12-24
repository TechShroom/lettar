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
package com.techshroom.lettar.annotation;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableListMultimap.toImmutableListMultimap;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.stream.Stream;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class AnnotationUtil {

    private static final Splitter EQUALS_LIM_2 = Splitter.on('=').limit(2);

    public static ImmutableMap<String, String> parseMap(String[] values) {
        return Stream.of(values).map(v -> {
            Iterator<String> split = EQUALS_LIM_2.split(v).iterator();
            String key = split.next();
            checkState(split.hasNext(), "invalid value %s", v);
            String value = split.next();
            return Maps.immutableEntry(key, value);
        }).collect(toImmutableMap(Entry::getKey, Entry::getValue));
    }

    public static ImmutableListMultimap<String, String> parseMultimap(String[] values) {
        return Stream.of(values).map(v -> {
            Iterator<String> split = EQUALS_LIM_2.split(v).iterator();
            String key = split.next();
            checkState(split.hasNext(), "invalid value %s", v);
            String value = split.next();
            return Maps.immutableEntry(key, value);
        }).collect(toImmutableListMultimap(Entry::getKey, Entry::getValue));
    }

}
