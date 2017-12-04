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
package com.techshroom.lettar.mime;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.Iterator;

import com.google.auto.value.AutoValue;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Simplified representation of a MIME type. Supports <code>*&#47;*</code>, for
 * the {@code Accept} header.
 */
@AutoValue
public abstract class MimeType {

    // cache of instance -> instance, uses key to set value to indicate cached
    // otherwise the key gets thrown away, so it takes no space :)
    private static final LoadingCache<MimeType, MimeType> MIME_CACHE = CacheBuilder.newBuilder()
            .maximumSize(50)
            .build(CacheLoader.from(k -> k));

    private static final Splitter SLASH = Splitter.on('/').limit(2);

    public static MimeType parse(String mimeType) {
        checkArgument(!mimeType.isEmpty(), "MIME type cannot be empty");
        // very loose mime definition
        // we don't really need strictness
        Iterator<String> parts = SLASH.split(mimeType).iterator();
        checkState(parts.hasNext(), "not even one part? broken Splitter?");
        String primary = parts.next();
        String secondary = parts.hasNext() ? parts.next() : "*";
        if (primary.equals("*")) {
            checkArgument(secondary.equals("*"), "only */* is allowed, not */%s", secondary);
        }
        return of(primary, secondary);
    }

    public static MimeType of(String primary, String secondary) {
        return MIME_CACHE.getUnchecked(new AutoValue_MimeType(primary, secondary));
    }

    MimeType() {
    }

    public abstract String getPrimaryType();

    public abstract String getSecondaryType();

    /**
     * Checks if this MIME type is a subtype of the provided MIME type.
     * 
     * @param mimeType
     *            - the MIME type to check against
     * @return {@code true} if this MIME type is a subtype of the provided MIME
     *         type
     */
    public final boolean matches(MimeType mimeType) {
        if (mimeType.getPrimaryType().equals("*")) {
            // matches all
            return true;
        }
        if (!mimeType.getPrimaryType().equals(getPrimaryType())) {
            return false;
        }
        String mtSec = mimeType.getSecondaryType();
        return mtSec.equals("*") || mtSec.equals(getSecondaryType());
    }

}
