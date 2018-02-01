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
package com.techshroom.lettar.pipe.builtins.codec;

import com.google.auto.service.AutoService;
import com.google.common.base.Throwables;
import com.techshroom.lettar.annotation.BodyCodec;
import com.techshroom.lettar.body.Codec;
import com.techshroom.lettar.inheiritor.Inheritor;
import com.techshroom.lettar.inheiritor.ReplacingInheritor;
import com.techshroom.lettar.pipe.Pipe;
import com.techshroom.lettar.pipe.builtins.decoder.DecoderPipe;
import com.techshroom.lettar.pipe.builtins.encoder.EncoderPipe;

@AutoService(Inheritor.class)
public class CodecInheritor extends ReplacingInheritor<Class<? extends Codec<?, ?, ?, ?>>, BodyCodec> {

    @Override
    public Pipe createPipe(Class<? extends Codec<?, ?, ?, ?>> data) {
        Codec<?, ?, ?, ?> codec;
        try {
            codec = data.newInstance();
        } catch (InstantiationException e) {
            Throwable t = e.getCause();
            Throwables.throwIfUnchecked(t);
            throw new RuntimeException(t);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("access denied to " + data.getName() + " constructor", e);
        }
        return new CodecPipe<>(new DecoderPipe<>(codec), new EncoderPipe<>(codec));
    }

    @Override
    protected Class<? extends Codec<?, ?, ?, ?>> interpretAnnotation(BodyCodec annotation) {
        return annotation.value();
    }

}
