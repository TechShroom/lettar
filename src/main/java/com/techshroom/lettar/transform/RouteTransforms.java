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
package com.techshroom.lettar.transform;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.ServiceLoader;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class RouteTransforms {

    // helper to reduce generic awkwardness...
    private static final RouteTransformFactory<?> RTF_WILDCARD = m -> ImmutableList.of();

    // map from Annotation class to RTF
    private static Map<Class<?>, RouteTransformFactory<?>> discoverFactories() {
        @SuppressWarnings("unchecked")
        ServiceLoader<RouteTransformFactory<?>> rtfs = (ServiceLoader<RouteTransformFactory<?>>) ServiceLoader.load(RTF_WILDCARD.getClass());
        ImmutableMap.Builder<Class<?>, RouteTransformFactory<?>> builder = ImmutableMap.builder();
        for (RouteTransformFactory<?> rtf : rtfs) {
            Class<?> annotation;
            String className = rtf.getClass().getCanonicalName();
            if (!className.endsWith("Factory")) {
                throw new IllegalStateException(RouteTransformFactory.class.getSimpleName() + 
                        " implementations must end their class name with Factory.");
            }
            int beforeFactory = className.lastIndexOf("Factory");
            String annotClassName = className.substring(0, beforeFactory);
            try {
                annotation = Class.forName(annotClassName);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Missing annotation class for " + rtf.getClass());
            }

            builder.put(annotation, rtf);
        }
        return builder.build();
    }

    private static final Map<Class<?>, RouteTransformFactory<?>> FACTORIES = discoverFactories();

    public static <A extends Annotation> ImmutableList<RouteTransform<?, ?, ?>> from(A annotation) {
        @SuppressWarnings("unchecked")
        RouteTransformFactory<A> factory = (RouteTransformFactory<A>) FACTORIES.get(annotation.getClass());
        if (factory == null) {
            return ImmutableList.of();
        }
        return factory.fromMarker(annotation);
    }

}
