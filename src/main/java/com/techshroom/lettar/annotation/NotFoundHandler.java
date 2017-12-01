package com.techshroom.lettar.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.techshroom.lettar.Request;

/**
 * Annotate a method with this to return a proper response when there is no
 * route for a given path. The method may take a {@link Request} as the first
 * parameter, or have no parameters.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotFoundHandler {

}
