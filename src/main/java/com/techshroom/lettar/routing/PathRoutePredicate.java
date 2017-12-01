/*
 * This file is part of Lettar, licensed under the MIT License (MIT).
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

import static com.google.common.base.Preconditions.checkState;

import java.util.BitSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.auto.value.AutoValue;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

public class PathRoutePredicate {

    @AutoValue
    public abstract static class MatchResult {

        public static MatchResult success(Iterable<String> parts) {
            return of(true, parts);
        }

        public static MatchResult fail() {
            return of(false, ImmutableList.of());
        }

        private static MatchResult of(boolean success, Iterable<String> parts) {
            return new AutoValue_PathRoutePredicate_MatchResult(success, ImmutableList.copyOf(parts));
        }

        MatchResult() {
        }

        public abstract boolean isSuccessfulMatch();

        public abstract ImmutableList<String> getParts();

    }

    private interface Part {

        int consume(List<String> parts, int index);

    }

    private static final class ConstantPart implements Part {

        private final String part;

        public ConstantPart(String part) {
            this.part = part;
        }

        @Override
        public int consume(List<String> parts, int index) {
            if (parts.get(index).equals(part)) {
                index++;
            }
            return index;
        }

    }

    private static final class RegexPart implements Part {

        private final Pattern regex;

        public RegexPart(Pattern regex) {
            this.regex = regex;
        }

        @Override
        public int consume(List<String> parts, int index) {
            String part = parts.get(index);
            Matcher matcher = regex.matcher(part);
            if (matcher.matches()) {
                index++;
            }
            return index;
        }

    }

    private enum WildcardPart implements Part {
        INSTANCE;

        @Override
        public int consume(List<String> parts, int index) {
            return index + 1;
        }
    }

    private enum EndChompPart implements Part {
        INSTANCE;

        @Override
        public int consume(List<String> parts, int index) {
            return parts.size();
        }
    }

    public static PathRoutePredicate parse(String route) {
        return new RouteParser(route).parse();
    }

    private static final class RouteParser {

        private static final Splitter SINGLE_COLON_SPLIT = Splitter.on(':').limit(2);

        private final List<String> parts;

        RouteParser(String route) {
            this.parts = Splitter.on('/').splitToList(route);
        }

        public PathRoutePredicate parse() {
            int numParts = 0;
            ImmutableList.Builder<Part> p = ImmutableList.builder();
            BitSet capturing = new BitSet(parts.size());

            boolean hasEndChomp = false;
            for (String part : parts) {
                if (part.length() == 0) {
                    // discard!
                    continue;
                }
                if (part.charAt(0) == '{' && part.charAt(part.length() - 1) == '}') {
                    // capturing!
                    capturing.set(numParts);
                    part = part.substring(1, part.length() - 1);
                }

                Part partObj = pickPart(part);
                if (partObj instanceof EndChompPart) {
                    checkState(!hasEndChomp, "only one '**' allowed per route");
                    hasEndChomp = true;
                }
                numParts++;
                p.add(partObj);
            }
            return new PathRoutePredicate(p.build(), capturing);
        }

        private Part pickPart(String part) {
            switch (part) {
                case "*":
                    return WildcardPart.INSTANCE;
                case "**":
                    return EndChompPart.INSTANCE;
                default:
                    break;
            }
            // we categorize by colon
            List<String> split = SINGLE_COLON_SPLIT.splitToList(part);
            if (split.size() > 1) {
                String category = split.get(0);
                String content = split.get(1);
                switch (category) {
                    case "re":
                        return new RegexPart(Pattern.compile(content));
                    case "static":
                        return new ConstantPart(content);
                    default:
                        // we do not allow unknown categories.
                        // to create a static match using a ':'
                        // prefix with 'static:'.
                        throw new IllegalStateException(String.format("Unknown category %s.", category)
                                + " Did you mean to create a static match containing a colon, with 'static:'?");
                }
            } else {
                return new ConstantPart(part);
            }
        }

    }

    private final List<Part> parts;
    private final BitSet capturing;

    private PathRoutePredicate(List<Part> parts, BitSet capturing) {
        this.parts = ImmutableList.copyOf(parts);
        this.capturing = (BitSet) capturing.clone();
    }

    public int getNumberOfCapturedParts() {
        return capturing.cardinality();
    }

    public MatchResult matches(List<String> path) {
        ImmutableList.Builder<String> p = ImmutableList.builder();
        int index = 0;
        for (int i = 0; i < parts.size(); i++) {
            if (index >= path.size()) {
                // we ran out of parts before path, we don't match
                return MatchResult.fail();
            }
            int nextIndex = parts.get(i).consume(path, index);
            nextIndex = Math.min(nextIndex, path.size());
            if (nextIndex <= index) {
                // part didn't match, failure
                return MatchResult.fail();
            }
            if (capturing.get(i)) {
                p.add(String.join("/", path.subList(index, nextIndex)));
            }
            index = nextIndex;
        }
        if (index < path.size()) {
            // we didn't match a full path, so no match
            return MatchResult.fail();
        }
        return MatchResult.success(p.build());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            boolean cap = capturing.get(i);
            if (cap) {
                sb.append('{');
            }
        }
        return sb.toString();
    }

}
