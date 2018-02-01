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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.base.Joiner;
import com.google.common.base.Joiner.MapJoiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public abstract class BaseFlowingElement<SELF extends FlowingElement<SELF>> implements FlowingElement<SELF> {

    public static class BFEBuilder<SELF extends FlowingElement<SELF>> implements FlowingElement.Builder<SELF> {

        private final Map<String, Optional<Object>> map;
        private final Function<ImmutableMap<String, Optional<Object>>, SELF> constructFunction;

        public BFEBuilder(Map<String, Optional<Object>> original, Function<ImmutableMap<String, Optional<Object>>, SELF> constructFunction) {
            this.map = new HashMap<>(original);
            this.constructFunction = constructFunction;
        }

        @Override
        public <V> Builder<SELF> put(Key<V> key, V value) {
            map.put(key.getId(), Optional.ofNullable(value));
            return this;
        }

        @Override
        public SELF build() {
            return constructFunction.apply(ImmutableMap.copyOf(map));
        }

    }

    private final ImmutableMap<String, Optional<Object>> map;

    public BaseFlowingElement(ImmutableMap<String, Optional<Object>> map) {
        this.map = map;
    }

    @Override
    public <V> V get(Key<V> key) {
        Optional<Object> opt = map.get(key.getId());
        if (opt == null || !opt.isPresent()) {
            return null;
        }
        @SuppressWarnings("unchecked")
        V value = (V) opt.get();
        return value;
    }

    protected abstract Function<ImmutableMap<String, Optional<Object>>, SELF> constructFunction();

    @Override
    public Builder<SELF> toBuilder() {
        return new BFEBuilder<>(map, constructFunction());
    }

    private static final MapJoiner MAP_JOINER = Joiner.on(',')
            .withKeyValueSeparator('=')
            .useForNull("null");

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("{");
        MAP_JOINER.appendTo(b,
                map.entrySet().stream()
                        .map(e -> {
                            return Maps.immutableEntry(e.getKey(), e.getValue().orElse(null));
                        })
                        .iterator());
        b.append('}');
        return b.toString();
    }

}
