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
package com.techshroom.lettar;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.techshroom.lettar.annotation.Produces;
import com.techshroom.lettar.transform.RouteTransform;
import com.techshroom.lettar.transform.RouteTransforms;

@AutoValue
public abstract class RouteEnhancements {

    @Nullable
    private static <A extends Annotation, T> Optional<T> annot(AnnotatedElement element, Class<A> annot, Function<A, T> extract) {
        A a = element.getAnnotation(annot);
        if (a == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(extract.apply(a));
    }

    public static RouteEnhancements load(AnnotatedElement element) {
        RouteEnhancements.Builder builder = new AutoValue_RouteEnhancements.Builder();
        builder.produces(annot(element, Produces.class, Produces::value)
                .orElse(new String[] { Produces.DEFAULT }));
        for (Annotation a : element.getAnnotations()) {
            builder.routeTransformsBuilder().addAll(RouteTransforms.from(a));
        }
        return builder.build();
    }

    @AutoValue.Builder
    public interface Builder {

        Builder produces(String[] produces);

        ImmutableList.Builder<RouteTransform<?, ?, ?>> routeTransformsBuilder();

        RouteEnhancements build();

    }

    public abstract ImmutableList<String> produces();

    public abstract ImmutableList<RouteTransform<?, ?, ?>> routeTransforms();

}
