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
package com.techshroom.lettar.pipe.builtins;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.techshroom.lettar.pipe.FlowingRequest;
import com.techshroom.lettar.pipe.InputPipe;
import com.techshroom.lettar.pipe.Key;
import com.techshroom.lettar.pipe.RequestKeys;
import com.techshroom.lettar.routing.PathRoutePredicate;
import com.techshroom.lettar.routing.PathRoutePredicate.MatchResult;

public class PathInputPipe implements InputPipe {

    public static final Key<ImmutableList<String>> parts = RequestKeys.path.child("parts");

    public static PathInputPipe create(PathRoutePredicate pathMatcher) {
        return new PathInputPipe(pathMatcher);
    }

    private final PathRoutePredicate pathMatcher;

    private PathInputPipe(PathRoutePredicate pathMatcher) {
        this.pathMatcher = pathMatcher;
    }

    @Override
    public FlowingRequest pipeIn(FlowingRequest request) {
        List<String> path = request.getPath();
        MatchResult match = pathMatcher.matches(path);
        if (!match.isSuccessfulMatch()) {
            return null;
        }
        return request.with(parts, match.getParts());
    }
}
