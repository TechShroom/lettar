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
package com.techshroom.lettar.inheiritor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.Iterators;

public class HashInheritorMap implements InheritorMap {

    public static HashInheritorMap create() {
        return new HashInheritorMap(new HashMap<>());
    }

    public static HashInheritorMap copyOf(InheritorMap map) {
        HashMap<Inheritor<?, ?>, Object> hashMap = new HashMap<>(map.getSize());
        map.forEach(e -> hashMap.put(e.getInheritor(), e.getOpaqueObject()));
        return new HashInheritorMap(hashMap);
    }

    private final Map<Inheritor<?, ?>, Object> map;

    private HashInheritorMap(Map<Inheritor<?, ?>, Object> map) {
        this.map = map;
    }

    @Override
    public Iterator<Entry> iterator() {
        return Iterators.transform(map.entrySet().iterator(), e -> InheritorMapEntry.create(e.getKey(), e.getValue()));
    }

    @Override
    public int getSize() {
        return map.size();
    }

    @Override
    public <O> O get(Inheritor<O, ?> key) {
        @SuppressWarnings("unchecked")
        O cast = (O) map.computeIfAbsent(key, Inheritor::getDefault);
        return cast;
    }

    @Override
    public <O> void put(Inheritor<O, ?> key, O opaque) {
        map.put(key, opaque);
    }

    @Override
    public String toString() {
        return map.toString();
    }

}
