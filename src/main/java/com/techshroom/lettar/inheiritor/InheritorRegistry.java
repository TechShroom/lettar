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
package com.techshroom.lettar.inheiritor;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ServiceLoader;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import com.google.common.reflect.TypeToken;

public class InheritorRegistry {

    private static final ImmutableMap<Class<?>, Inheritor<?, ?>> INHERITORS;
    private static final ImmutableList<Inheritor<?, ?>> REQUIRED_INHERITORS;
    static {
        INHERITORS = Streams.stream(ServiceLoader.load(Inheritor.class))
                .map(i -> (Inheritor<?, ?>) i)
                .collect(toImmutableMap(i -> unpackAnnotationType(i.getClass()), Function.identity()));
        REQUIRED_INHERITORS = INHERITORS.values().stream()
                .filter(i -> i.getClass().getAnnotation(Required.class) != null)
                .collect(toImmutableList());
    }

    @SuppressWarnings("rawtypes")
    private static Class<?> unpackAnnotationType(Class<? extends Inheritor> inheritorClass) {
        TypeToken<? extends Inheritor> token = TypeToken.of(inheritorClass);
        TypeToken<?> inheritorInfo = token.getSupertype(Inheritor.class);
        Type inhType = inheritorInfo.getType();
        checkState(inhType instanceof ParameterizedType, "inheritor type incorrectly resolved: %s", inhType.getClass());
        Type annotType = ((ParameterizedType) inhType).getActualTypeArguments()[1];
        checkState(annotType instanceof Class, "annotation type incorrect: %s (%s)", annotType, annotType.getClass());
        return (Class<?>) annotType;
    }

    public static ImmutableList<Inheritor<?, ?>> getRequiredInheritors() {
        return REQUIRED_INHERITORS;
    }

    public static ImmutableMap<Class<?>, Inheritor<?, ?>> getInheritors() {
        return INHERITORS;
    }

    public static <O, A extends Annotation> Inheritor<O, A> getInheritor(Class<A> inheritorClass) {
        @SuppressWarnings("unchecked")
        Inheritor<O, A> cast = (Inheritor<O, A>) INHERITORS.get(inheritorClass);
        return cast;
    }

}
