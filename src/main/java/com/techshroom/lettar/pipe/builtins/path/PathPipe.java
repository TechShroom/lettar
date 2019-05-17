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
package com.techshroom.lettar.pipe.builtins.path;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.techshroom.lettar.pipe.FilterPipe;
import com.techshroom.lettar.pipe.FlowingRequest;
import com.techshroom.lettar.pipe.InputPipe;
import com.techshroom.lettar.pipe.Key;
import com.techshroom.lettar.pipe.RequestKeys;
import com.techshroom.lettar.routing.PathRoutePredicate;
import com.techshroom.lettar.routing.PathRoutePredicate.MatchResult;

import static com.google.common.base.Preconditions.checkState;

public class PathPipe implements FilterPipe, InputPipe {

    public static final Key<ImmutableList<String>> parts = RequestKeys.path.child("parts");

    public static PathPipe create(List<PathRoutePredicate> pathMatcher) {
        return new PathPipe(ImmutableList.copyOf(pathMatcher));
    }

    private final ImmutableList<PathRoutePredicate> pathMatcher;

    private Optional<MatchResult> getMatchResult(FlowingRequest request) {
        List<String> path = request.getPath();
        return pathMatcher.stream()
            .map(p -> p.matches(path))
            .filter(MatchResult::isSuccessfulMatch)
            .findAny();
    }

    private PathPipe(ImmutableList<PathRoutePredicate> pathMatcher) {
        this.pathMatcher = pathMatcher;
    }

    @Override
    public FlowingRequest pipeIn(FlowingRequest request) {
        Optional<MatchResult> match = getMatchResult(request);
        checkState(match.isPresent(), "Request accepted that did not match: %s", request);
        return request.with(parts, match.get().getParts());
    }

    @Override
    public boolean accepts(FlowingRequest request) {
        return getMatchResult(request).isPresent();
    }

    @Override
    public String toString() {
        return "Path{" + pathMatcher + "}";
    }
}
