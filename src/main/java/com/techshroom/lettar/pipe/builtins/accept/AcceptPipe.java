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
package com.techshroom.lettar.pipe.builtins.accept;

import java.util.Optional;

import com.techshroom.lettar.mime.MimeType;
import com.techshroom.lettar.pipe.BiPipe;
import com.techshroom.lettar.pipe.FlowingRequest;
import com.techshroom.lettar.pipe.FlowingResponse;
import com.techshroom.lettar.pipe.Key;
import com.techshroom.lettar.pipe.ResponseKeys;
import com.techshroom.lettar.routing.AcceptPredicate;

public class AcceptPipe implements BiPipe {

    public static final Key<MimeType> contentType = Key.of("Request.contentType");

    public static AcceptPipe create(AcceptPredicate acceptMatcher) {
        return new AcceptPipe(acceptMatcher);
    }

    private final AcceptPredicate acceptMatcher;

    private AcceptPipe(AcceptPredicate acceptMatcher) {
        this.acceptMatcher = acceptMatcher;
    }

    @Override
    public FlowingRequest pipeIn(FlowingRequest request) {
        String accept = request.getHeaders().getSingleValueOrDefault("accept", "*/*");
        Optional<MimeType> match = acceptMatcher.matches(accept);
        if (!match.isPresent()) {
            return null;
        }
        return request.with(contentType, match.get());
    }

    @Override
    public FlowingResponse pipeOut(FlowingResponse response) {
        MimeType contentType = response.getRequest().get(AcceptPipe.contentType);
        return response.modify(ResponseKeys.headers, headers -> headers.setIfAbsent("content-type", contentType.toString()));
    }

    @Override
    public String toString() {
        return "Accept{" + acceptMatcher + "}";
    }
}
