package com.techshroom.lettar.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.techshroom.lettar.body.Codec;

@Documented
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
public @interface BodyCodec {

    Class<? extends Codec<?, ?, ?, ?>> value();
}