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
package com.techshroom.lettar.pipe.builtins.encoder;

import com.techshroom.lettar.body.Encoder;
import com.techshroom.lettar.pipe.FlowingResponse;
import com.techshroom.lettar.pipe.OutputPipe;
import com.techshroom.lettar.pipe.ResponseKeys;

public class EncoderPipe<I, O> implements OutputPipe {

    private final Encoder<I, O> encoder;

    public EncoderPipe(Encoder<I, O> encoder) {
        this.encoder = encoder;
    }

    @Override
    public FlowingResponse pipeOut(FlowingResponse response) {
        I input = response.getBody();
        O output = encoder.encode(input);
        return response.with(ResponseKeys.body(), output);
    }

    @Override
    public String toString() {
        return encoder.toString();
    }
}
