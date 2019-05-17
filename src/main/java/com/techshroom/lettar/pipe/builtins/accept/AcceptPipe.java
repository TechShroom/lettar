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

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.techshroom.lettar.mime.AcceptMime;
import com.techshroom.lettar.mime.MimeType;
import com.techshroom.lettar.pipe.BiPipe;
import com.techshroom.lettar.pipe.FilterPipe;
import com.techshroom.lettar.pipe.FlowingRequest;
import com.techshroom.lettar.pipe.FlowingResponse;
import com.techshroom.lettar.pipe.Key;
import com.techshroom.lettar.pipe.ResponseKeys;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;

public class AcceptPipe implements FilterPipe, BiPipe {

    // Limit number of splits to prevent attacks -- there shouldn't be this many
    private static final Splitter ACCEPT_SPLITTER = Splitter.on(',').limit(50);
    public static final Key<MimeType> contentType = Key.of("Request.contentType");

    public static AcceptPipe create(Iterable<MimeType> providedTypes, @Nullable MimeType defaultType) {
        return new AcceptPipe(ImmutableList.copyOf(providedTypes), defaultType);
    }

    private final ImmutableList<MimeType> providedTypes;
    private final @Nullable MimeType defaultType;

    private AcceptPipe(ImmutableList<MimeType> providedTypes, MimeType defaultType) {
        this.providedTypes = providedTypes;
        this.defaultType = defaultType;
    }

    private Optional<MimeType> getAcceptedMimeType(FlowingRequest request) {
        String accept = request.getHeaders().getSingleValueOrDefault("accept", "*/*");
        List<MimeType> acceptTypes = getAcceptTypes(accept);
        return negotiateContentType(acceptTypes, providedTypes);
    }

    @Override
    public FlowingRequest pipeIn(FlowingRequest request) {
        Optional<MimeType> accepted = getAcceptedMimeType(request);
        checkState(accepted.isPresent(), "Request accepted that did not match: %s", request);
        return request.with(contentType, accepted.get());
    }

    @Override
    public boolean accepts(FlowingRequest request) {
        return getAcceptedMimeType(request).isPresent();
    }

    private List<MimeType> getAcceptTypes(String accept) {
        return ImmutableList.copyOf(
            ImmutableList.copyOf(ACCEPT_SPLITTER.split(accept)).stream()
                .map(AcceptMime::parse)
                .sorted()
                .map(AcceptMime::getMimeType)
                .iterator()
        );
    }

    private Optional<MimeType> negotiateContentType(List<MimeType> acceptedTypes, List<MimeType> providedTypes) {
        for (MimeType acceptable : acceptedTypes) {
            for (MimeType provided : providedTypes) {
                if (provided.isAssignableTo(acceptable)) {
                    return Optional.of(provided);
                }
            }
        }
        return Optional.ofNullable(defaultType);
    }

    @Override
    public FlowingResponse pipeOut(FlowingResponse response) {
        MimeType contentType = response.getRequest().get(AcceptPipe.contentType);
        return response.modify(ResponseKeys.headers, headers -> headers.setIfAbsent("content-type", contentType.toString()));
    }

    @Override
    public String toString() {
        return "Accept{" + providedTypes + "}";
    }
}
