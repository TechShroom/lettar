package com.techshroom.lettar.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.techshroom.lettar.routing.HttpMethod;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Route {

    String[] path();

    HttpMethod[] method() default HttpMethod.GET;

    String[] params() default {};

    String[] headers() default {};

}
