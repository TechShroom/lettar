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
package com.techshroom.lettar.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.common.collect.ImmutableList;
import com.techshroom.lettar.annotation.BodyCodec.Marker;
import com.techshroom.lettar.body.Codec;
import com.techshroom.lettar.reflect.Constructors;
import com.techshroom.lettar.transform.RouteTransform;
import com.techshroom.lettar.transform.RouteTransformFactory;

/**
 * Sets the codec for the body content. This follows the same rules as using a
 * {@link BodyEncoder} and {@link BodyDecoder}.
 */
public class BodyCodec implements RouteTransformFactory<Marker> {

    @Documented
    @Retention(RUNTIME)
    @Target({ TYPE, METHOD })
    public @interface Marker {

        Class<? extends Codec<?, ?, ?, ?>> value();
    }

    @Override
    public ImmutableList<RouteTransform<?, ?>> fromMarker(Marker marker) {
        @SuppressWarnings("unchecked")
        Codec<Object, Object, Object, Object> codec = (Codec<Object, Object, Object, Object>) Constructors.instatiate(marker.value());
        return ImmutableList.of(new BodyEncoder(codec), new BodyDecoder(codec));
    }

}