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

import com.google.auto.service.AutoService;
import com.techshroom.lettar.Response;
import com.techshroom.lettar.body.Decoder;
import com.techshroom.lettar.reflect.Constructors;
import com.techshroom.lettar.routing.Request;
import com.techshroom.lettar.transform.RouteTransform;
import com.techshroom.lettar.transform.TransformChain;

/**
 * Sets the decoder for the body content. The accepted request from the method
 * can contain anything that the decoder returns.
 */
@AutoService(RouteTransform.class)
public class BodyDecoderFactory implements RouteTransform<Object, Object, Object> {

    public static BodyDecoderFactory fromMarker(BodyDecoder marker) {
        @SuppressWarnings("unchecked")
        Decoder<Object, Object> decoder = (Decoder<Object, Object>) Constructors.instatiate(marker.value());
        return new BodyDecoderFactory(decoder);
    }

    private final Decoder<Object, Object> decoder;

    BodyDecoderFactory(Decoder<Object, Object> decoder) {
        this.decoder = decoder;
    }

    @Override
    public Response<Object> transform(TransformChain<Object, Object> chain) {
        Request<Object> req = chain.request();
        return chain.withRequest(req.withBody(decoder.decode(req.getBody()))).next();
    }
    
    @Override
    public boolean acceptRequest(Request<Object> request) {
        return RouteTransform.super.acceptRequest(request);
    }
}
