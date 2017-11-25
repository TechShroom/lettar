package com.techshroom.lettar.reflect;

import java.util.Iterator;

import com.google.common.collect.AbstractIterator;

public class ClassClimbing {

    public static Iterator<Class<?>> superClasses(Class<?> start) {
        return superClasses(start, Object.class);
    }

    public static Iterator<Class<?>> superClasses(Class<?> start, Class<?> top) {
        return new AbstractIterator<Class<?>>() {

            private Class<?> current = start;

            @Override
            protected Class<?> computeNext() {
                Class<?> ret = current;
                if (ret == null || !top.isAssignableFrom(ret)) {
                    return endOfData();
                }
                current = ret.getSuperclass();
                return ret;
            }
        };
    }

}
