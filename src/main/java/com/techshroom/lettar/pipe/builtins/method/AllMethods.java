package com.techshroom.lettar.pipe.builtins.method;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.techshroom.lettar.pipe.PipeCompatible;
import com.techshroom.lettar.routing.HttpMethod;

/**
 * Helper annotation for {@link Method}, that indicates all methods should be
 * allowed.
 */
@Documented
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
@PipeCompatible(metaAnnotation = true)
@Method(HttpMethod.CONNECT)
@Method(HttpMethod.DELETE)
@Method(HttpMethod.GET)
@Method(HttpMethod.HEAD)
@Method(HttpMethod.OPTIONS)
@Method(HttpMethod.PATCH)
@Method(HttpMethod.POST)
@Method(HttpMethod.PUT)
public @interface AllMethods {

}
