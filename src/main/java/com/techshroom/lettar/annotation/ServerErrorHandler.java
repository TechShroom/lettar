package com.techshroom.lettar.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.techshroom.lettar.Request;

/**
 * Annotate a method with this to return a proper response when an exception
 * occurs. The method may take a {@link Request} as the first parameter. It may
 * also take any of the exception types declared in {@link #exception()}, or a
 * supertype of any of them. Each exception type will be mapped to one
 * parameter, and only that parameter (and the request parameter) will be
 * supplied with a value when the matching exception is thrown. The other
 * parameters will be null.
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface ServerErrorHandler {

    Class<? extends Throwable>[] exception() default Exception.class;

}
