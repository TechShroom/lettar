/*
 * This file is part of Lettar, licensed under the MIT License (MIT).
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
package com.techshroom.lettar.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.techshroom.lettar.Request;
import com.techshroom.lettar.Response;
import com.techshroom.lettar.mime.MimeType;
import com.techshroom.lettar.routing.HttpMethod;

/**
 * Indicates that the annotated method is a route method. The fields of this
 * annotation will be used to determine if a request matches, and if so, the
 * annotated method will be called with information.
 * 
 * <p>
 * Methods annotated must be of the following form:
 * <ul>
 * <li>Return Type: {@link Response}</li>
 * <li>Parameters: First parameter may be {@link Request}. The parameter after
 * that may be {@link MimeType}, for the content type. Following parameters will
 * be filled with captured {@link #path()} values. The type of the path
 * parameters may be most primitive types, and String.</li>
 * </ul>
 * </p>
 * <p>
 * Example method forms:
 * <ul>
 * <li>{@code Response example1(String someParameter)}</li>
 * <li>{@code Response example2(Request request, String someParameter)}</li>
 * <li>{@code Response example3(MimeType mimeType, String someParameter)}</li>
 * <li>{@code Response example4(Request request, MimeType mimeType, String
 * someParameter)}</li>
 * </ul>
 * </p>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Route {

    /**
     * The path(s) to match. The format allows for wildcards (*, **) and regular
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
     * <li>{@code /foo/{*} - captures the content of the wildcard, captures can
     * be used for any type of part</li>
     * </ul>
     * </p>
     */
    String[] path();

    /**
     * The method(s) to match.
     */
    HttpMethod[] method() default HttpMethod.GET;

    /**
     * The query parameters to match. Each value should be of the format
     * {@code key=value}.
     */
    String[] params() default {};

    /**
     * The headers to match. Each value should be of the format
     * {@code key=value}.
     * 
     * <p>
     * Note: for the {@code Accept} header, see {@link Produces} for better
     * matching.
     * </p>
     */
    String[] headers() default {};

    // TODO: precedence matching -- a "more exact" route will be chosen first
    // what is a "more exact" route? more parts to match?
    // does this include captures?

}
