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
package com.techshroom.lettar.pipe;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.techshroom.lettar.inheiritor.HashInheritorMap;
import com.techshroom.lettar.inheiritor.Inheritor;
import com.techshroom.lettar.inheiritor.InheritorMap;
import com.techshroom.lettar.inheiritor.InheritorRegistry;

class InheritanceHelper {

    private static final class AnnotationList<A extends Annotation> {

        private final Class<A> annotationClass;
        private final ImmutableList<A> annotations;

        public AnnotationList(Class<A> annotationClass, ImmutableList<A> annotations) {
            this.annotationClass = annotationClass;
            this.annotations = annotations;
        }

    }

    private static List<AnnotationList<?>> interpretAnnotations(Annotation[] annotations) {
        Map<Class<? extends Annotation>, ImmutableList.Builder<Annotation>> annoMap = new HashMap<>();
        getPipeAnnotations(annotations).forEach(annotation -> {
            annoMap.computeIfAbsent(annotation.annotationType(), k -> ImmutableList.builder())
                    .add(annotation);
        });
        return annoMap.entrySet().stream()
                .map(e -> {
                    @SuppressWarnings("unchecked")
                    Class<Annotation> key = (Class<Annotation>) e.getKey();
                    return new AnnotationList<Annotation>(key, e.getValue().build());
                })
                .collect(toImmutableList());
    }

    private static Stream<Annotation> getPipeAnnotations(Annotation[] annotations) {
        return Stream.of(annotations)
                .flatMap(a -> {
                    PipeCompatible pc = a.annotationType().getAnnotation(PipeCompatible.class);
                    if (pc == null) {
                        return Stream.of();
                    }
                    if (pc.metaAnnotation()) {
                        return getPipeAnnotations(a.annotationType().getAnnotations());
                    }
                    return Stream.of(a);
                });
    }

    private static void applyInheritance(InheritorMap inheritanceMap, AnnotatedElement annotated) {
        List<AnnotationList<?>> annotationLists = interpretAnnotations(annotated.getAnnotations());
        for (AnnotationList<?> list : annotationLists) {
            applyInheritance(inheritanceMap, list);
        }
    }

    private static <O, A extends Annotation> void applyInheritance(InheritorMap inheritanceMap, AnnotationList<A> list) {
        Inheritor<O, A> inheritor = InheritorRegistry.getInheritor(list.annotationClass);
        checkState(inheritor != null, "Annotation missing inheritor: %s", list.annotationClass.getName());
        O existing = inheritanceMap.get(inheritor);
        O next = inheritor.interpretAnnotations(list.annotations);
        O merged = inheritor.inherit(existing, next);
        inheritanceMap.put(inheritor, merged);
    }

    private final InheritorMap inheritorMap;

    public InheritanceHelper(InheritorMap inheritorMap) {
        this.inheritorMap = inheritorMap;
        putDefaults();
    }

    private void putDefaults() {
        InheritorRegistry.getRequiredInheritors().forEach(inheritor -> {
            Object o = this.inheritorMap.get(inheritor);
            if (o == null) {
                putDefault(inheritor);
            }
        });
    }

    private <O> void putDefault(Inheritor<O, ?> inheritor) {
        this.inheritorMap.put(inheritor, inheritor.getDefault());
    }

    public InheritorMap getInheritorMap() {
        return inheritorMap;
    }

    public InheritanceHelper inherit(AnnotatedElement element) {
        InheritorMap copy = HashInheritorMap.copyOf(inheritorMap);
        applyInheritance(copy, element);
        return new InheritanceHelper(copy);
    }

}
