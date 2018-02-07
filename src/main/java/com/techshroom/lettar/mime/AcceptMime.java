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

import java.util.Comparator;
import java.util.List;

import com.google.auto.value.AutoValue;
import com.google.common.base.Splitter;
import com.google.common.base.Splitter.MapSplitter;

@AutoValue
public abstract class AcceptMime implements Comparable<AcceptMime> {

    private static final Splitter SEMICOLON = Splitter.on(';').limit(2).trimResults();
    // Limit number of results to prevent attacks
    // there should only be a handful of parameters
    private static final MapSplitter MIME_Q_SPLIT = Splitter.on(';').limit(50).trimResults().withKeyValueSeparator('=');

    public static AcceptMime parse(String mimePlusQuality) {
        List<String> parts = SEMICOLON.splitToList(mimePlusQuality);
        MimeType mime = MimeType.parse(parts.get(0));
        double quality = 1;
        if (parts.size() > 1) {
            try {
                quality = Double.parseDouble(MIME_Q_SPLIT.split(parts.get(1)).getOrDefault("q", "1"));
            } catch (NumberFormatException ignored) {
                // no quality input
            }
        }
        return new AutoValue_AcceptMime(mime, quality);
    }

    AcceptMime() {
    }

    public abstract MimeType getMimeType();

    public abstract double getQuality();

    private static int starCompare(String a, String b) {
        boolean aStar = a.equals("*");
        boolean bStar = b.equals("*");
        if (aStar && bStar) {
            return 0;
        } else if (aStar) {
            return 1;
        } else if (bStar) {
            return -1;
        }
        return 0;
    }

    private static final Comparator<AcceptMime> COMP_DELEGATE = Comparator
            .<AcceptMime> comparingDouble(AcceptMime::getQuality)
            .thenComparing(am -> am.getMimeType().getPrimaryType(), AcceptMime::starCompare)
            .thenComparing(am -> am.getMimeType().getSecondaryType(), AcceptMime::starCompare);

    @Override
    public int compareTo(AcceptMime o) {
        return COMP_DELEGATE.compare(this, o);
    }
}