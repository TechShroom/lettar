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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.google.auto.value.AutoValue;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.techshroom.lettar.annotation.Route;
import com.techshroom.lettar.mime.MimeType;
import com.techshroom.lettar.routing.PathRoutePredicate.MatchResult;

/**
 * Runtime interpretation of {@link Route}.
 * 
 * @param <V>
 *            - the type contained in this route
 */
@AutoValue
public abstract class RuntimeRoute<V> {

    public static <V> RuntimeRoute<V> of(HttpMethodPredicate methodPredicate,
            Collection<PathRoutePredicate> pathPredicates,
            KeyValuePredicate paramPredicate,
            KeyValuePredicate headerPredicate,
            AcceptPredicate acceptPredicate,
            V target) {
        return new AutoValue_RuntimeRoute<>(methodPredicate, ImmutableSet.copyOf(pathPredicates),
                paramPredicate, headerPredicate, acceptPredicate, target);
    }

    private static final Splitter SLASH = Splitter.on('/').omitEmptyStrings();

    RuntimeRoute() {
    }

    abstract HttpMethodPredicate methodPredicate();

    abstract ImmutableSet<PathRoutePredicate> pathPredicates();

    abstract KeyValuePredicate paramPredicate();

    abstract KeyValuePredicate headerPredicate();

    abstract AcceptPredicate acceptPredicate();

    abstract V target();

    public final Optional<RouteResult<V>> matches(Request request) {
        if (!methodPredicate().matches(request.getMethod())) {
            return Optional.empty();
        }
        if (!paramPredicate().matches(request.getQueryParts().getMultimap())) {
            return Optional.empty();
        }
        if (!headerPredicate().matches(request.getHeaders().getMultimap())) {
            return Optional.empty();
        }
        String acceptHeaderValue = request.getHeaders().getSingleValueOrDefault("Accept", "*/*");
        Optional<MimeType> contentType = acceptPredicate().matches(acceptHeaderValue);
        if (!contentType.isPresent()) {
            return Optional.empty();
        }
        List<String> splitPath = SLASH.splitToList(request.getPath());
        Optional<MatchResult> result = pathPredicates().stream()
                .map(p -> p.matches(splitPath))
                .filter(MatchResult::isSuccessfulMatch)
                .findFirst();
        if (!result.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(RouteResult.of(target(), result.get().getParts(), contentType.get()));
    }

}
