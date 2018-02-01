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
package com.techshroom.lettar.pipe.builtins.path;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.techshroom.lettar.pipe.PipeCompatible;

/**
 * The path to match. The format allows for wildcards (*, **) and regular
 * expressions in the path components.
 * 
 * <p>
 * Examples:
 * <ul>
 * <li>{@code /foo/bar}</li>
 * <li>{@code /foo/*} - matches {@code /foo/something}, but not
 * {@code /foo/something/else}</li>
 * <li>{@code /foo/**} - matches both of the above</li>
 * <li>{@code /foo/*}{@code /else} - matches just
 * {@code /foo/something/else}</li>
 * <li>{@code /foo/re:.+?} - regular expressions must be prefixed with
 * {@code re:}</li>
 * <li>{@code /foo/{*}} - captures the content of the wildcard, captures can be
 * used for any type of part</li>
 * </ul>
 * </p>
 */
@Documented
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
@PipeCompatible
@Repeatable(PathMultiple.class)
public @interface Path {

    String value();

}
