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
package com.techshroom.lettar.reflect;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;

/**
 * Functions to be called into by method handle conversions. They validate their
 * arguments and throw a specific class of exception for handling by 404
 * handlers.
 */
public class StringConverters {

    private static final Map<Class<?>, MethodHandle> HANDLES;

    private static MethodHandle getMethodHandle(String string) {
        try {
            Method method = StringConverters.class.getMethod(string, String.class);
            return MethodHandles.publicLookup().unreflect(method);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    static {
        ImmutableMap.Builder<Class<?>, MethodHandle> handles = ImmutableMap.builder();

        handles.put(String.class, getMethodHandle("string"));
        handles.put(Boolean.class, getMethodHandle("bool"));
        handles.put(Byte.class, getMethodHandle("byte_"));
        handles.put(Double.class, getMethodHandle("doubleFloating"));
        handles.put(Float.class, getMethodHandle("floating"));
        handles.put(Integer.class, getMethodHandle("integer"));
        handles.put(Long.class, getMethodHandle("longInteger"));
        handles.put(Short.class, getMethodHandle("shortInteger"));

        HANDLES = handles.build();
    }

    public static MethodHandle getHandleForArgType(Class<?> type) {
        MethodHandle handle = HANDLES.get(Primitives.wrap(type));
        checkArgument(handle != null, "%s is not a convertible type", type);
        return handle;
    }

    public static String string(String arg) throws StringConversionException {
        return arg;
    }

    public static boolean bool(String arg) throws StringConversionException {
        try {
            return Boolean.valueOf(arg);
        } catch (NumberFormatException e) {
            throw new StringConversionException(e);
        }
    }

    public static byte byte_(String arg) throws StringConversionException {
        try {
            return Byte.valueOf(arg);
        } catch (NumberFormatException e) {
            throw new StringConversionException(e);
        }
    }

    public static double doubleFloating(String arg) throws StringConversionException {
        try {
            return Double.valueOf(arg);
        } catch (NumberFormatException e) {
            throw new StringConversionException(e);
        }
    }

    public static float floating(String arg) throws StringConversionException {
        try {
            return Float.valueOf(arg);
        } catch (NumberFormatException e) {
            throw new StringConversionException(e);
        }
    }

    public static int integer(String arg) throws StringConversionException {
        try {
            return Integer.valueOf(arg);
        } catch (NumberFormatException e) {
            throw new StringConversionException(e);
        }
    }

    public static long longInteger(String arg) throws StringConversionException {
        try {
            return Long.valueOf(arg);
        } catch (NumberFormatException e) {
            throw new StringConversionException(e);
        }
    }

    public static short shortInteger(String arg) throws StringConversionException {
        try {
            return Short.valueOf(arg);
        } catch (NumberFormatException e) {
            throw new StringConversionException(e);
        }
    }

}
