package com.techshroom.lettar.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The MIME types that a {@link Route} may produce. These will be properly
 * matched against the {@code Accept} header, and the proper
 * {@code Content-type} will be automatically added if absent. This may also be
 * added to a class to indicate the content of all routes inside.
 */
@Documented
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
public @interface Produces {

    String DEFAULT = "application/octet-stream";

    String[] value() default DEFAULT;

}
