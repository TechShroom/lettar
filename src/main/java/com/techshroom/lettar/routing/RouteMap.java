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
package com.techshroom.lettar.routing;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.ImmutableSet;

public class RouteMap<V> {

    /**
     * Routes moves through two states, first modifiable for registration, then
     * immutablized on first routing.
     */
    private transient Set<RuntimeRoute<V>> routes = new HashSet<>();
    private boolean routeImmutable = false;

    public void addRuntimeRoute(RuntimeRoute<V> route) {
        if (routeImmutable) {
            throw new UnsupportedOperationException("Routes may not be added after routing.");
        }
        routes.add(route);
    }

    public <O> Optional<O> route(Request request, Function<RouteResult<V>, Optional<O>> handler) {
        if (!routeImmutable) {
            routes = ImmutableSet.copyOf(routes);
            routeImmutable = true;
        }
        Iterator<RouteResult<V>> map = routes.stream()
                // filter by matches
                .map(r -> r.matches(request))
                // optional unpack
                .filter(Optional::isPresent)
                .map(Optional::get)
                .iterator();
        while (map.hasNext()) {
            RouteResult<V> route = map.next();
            Optional<O> result = handler.apply(route);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

}
