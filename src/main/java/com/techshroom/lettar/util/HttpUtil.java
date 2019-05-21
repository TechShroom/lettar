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
package com.techshroom.lettar.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.techshroom.lettar.collections.HttpMultimap;

import java.util.List;

/**
 * Utilities for HTTP.
 */
public class HttpUtil {

    /**
     * Creates a special {@link ImmutableMultimap} builder that creates
     * case-insensitive maps, as required for headers.
     *
     * <p>
     * Do <em>not</em> use the resulting map for any further manipulation.
     * Pass it directly to create a new {@link HttpMultimap}, which will ensure
     * proper key case checking.
     * </p>
     *
     * @return a header map builder
     */
    public static ImmutableListMultimap.Builder<String, String> headerMultimapBuilder() {
        return ImmutableListMultimap.<String, String>builder()
            .orderKeysBy(String.CASE_INSENSITIVE_ORDER);
    }

    /**
     * Creates a special {@link ImmutableSortedMap} builder that creates
     * case-insensitive maps, as required for headers.
     *
     * @return a header map builder
     */
    public static ImmutableSortedMap.Builder<String, ImmutableList<String>> headerMapBuilder() {
        return ImmutableSortedMap.orderedBy(String.CASE_INSENSITIVE_ORDER);
    }

    /**
     * Creates a special {@link ImmutableSortedMap} builder that creates
     * case-insensitive maps, as required for headers.
     *
     * @return a header map builder
     */
    public static ImmutableSortedMap.Builder<String, String> singleHeaderMapBuilder() {
        return ImmutableSortedMap.orderedBy(String.CASE_INSENSITIVE_ORDER);
    }

}
