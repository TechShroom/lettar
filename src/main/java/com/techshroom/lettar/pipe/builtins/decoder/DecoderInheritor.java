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
package com.techshroom.lettar.pipe.builtins.decoder;

import com.google.auto.service.AutoService;
import com.techshroom.lettar.annotation.BodyDecoder;
import com.techshroom.lettar.body.Decoder;
import com.techshroom.lettar.inheiritor.Inheritor;
import com.techshroom.lettar.inheiritor.ReplacingInheritor;
import com.techshroom.lettar.pipe.Pipe;
import com.techshroom.lettar.reflect.Constructors;

@AutoService(Inheritor.class)
public class DecoderInheritor extends ReplacingInheritor<Class<? extends Decoder<?, ?>>, BodyDecoder> {

    @Override
    public Pipe createPipe(Class<? extends Decoder<?, ?>> data) {
        Decoder<?, ?> dec = Constructors.instatiate(data);
        return new DecoderPipe<>(dec);
    }

    @Override
    protected Class<? extends Decoder<?, ?>> interpretAnnotation(BodyDecoder annotation) {
        return annotation.value();
    }

}
