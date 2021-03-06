/*
 * This file is part of lettar, licensed under the MIT License (MIT).
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
package com.techshroom.lettar.pipe.builtins.accept;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.techshroom.lettar.pipe.PipeCompatible;

/**
 * The MIME types that a route may produce. These will be properly matched
 * against the {@code Accept} header, and the proper {@code Content-type} will
 * be automatically added if absent. This may also be added to a class to
 * indicate the content of all routes inside.
 */
@Documented
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
@PipeCompatible
@Repeatable(ProducesMultiple.class)
public @interface Produces {

    /**
     * Use this for {@link #value()} to indicate that it should not be used for
     * matching or selection.
     */
    static String UNSPECIFIED = "Unspecified content type.";

    String value();

    /**
     * If {@code true}, this route will match any {@code Accept} header,
     * regardless of actual types specified. The content-type will still be set
     * according to {@link #value()}.
     * 
     * @return {@code true} if this route matches any {@code Accept} header
     */
    boolean matchesAnything() default false;

}
