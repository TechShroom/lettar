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
