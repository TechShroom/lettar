package com.techshroom.lettar.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.techshroom.lettar.pipe.PipeCompatible;
import com.techshroom.lettar.pipe.builtins.accept.Produces;
import com.techshroom.lettar.pipe.builtins.method.AllMethods;

@Documented
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
@PipeCompatible(metaAnnotation = true)
@AllMethods
@Produces(value = Produces.UNSPECIFIED, matchesAnything = true)
public @interface AnythingGoes {

}
