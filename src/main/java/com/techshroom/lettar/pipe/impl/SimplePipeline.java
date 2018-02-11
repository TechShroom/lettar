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
package com.techshroom.lettar.pipe.impl;

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;
import com.techshroom.lettar.Logging;
import com.techshroom.lettar.pipe.FlowingRequest;
import com.techshroom.lettar.pipe.FlowingResponse;
import com.techshroom.lettar.pipe.Handler;
import com.techshroom.lettar.pipe.InputPipe;
import com.techshroom.lettar.pipe.OutputPipe;
import com.techshroom.lettar.pipe.Pipeline;

public class SimplePipeline implements Pipeline {

    private static final Logger LOGGER = Logging.getLogger();

    // TODO if we want performance, this could _easily_ be redone with
    // MethodHandles

    public static SimplePipeline create(List<InputPipe> inputPipes,
            Handler handler,
            List<OutputPipe> outputPipes) {
        return new SimplePipeline(ImmutableList.copyOf(inputPipes), handler, ImmutableList.copyOf(outputPipes));
    }

    private final ImmutableList<InputPipe> inputPipes;
    private final Handler handler;
    private final ImmutableList<OutputPipe> outputPipes;

    private SimplePipeline(ImmutableList<InputPipe> inputPipes,
            Handler handler,
            ImmutableList<OutputPipe> outputPipes) {
        this.inputPipes = inputPipes;
        this.handler = handler;
        this.outputPipes = outputPipes;
    }

    @Override
    public ImmutableList<InputPipe> getInputPipes() {
        return inputPipes;
    }

    @Override
    public Handler getHandler() {
        return handler;
    }

    @Override
    public ImmutableList<OutputPipe> getOutputPipes() {
        return outputPipes;
    }

    @Override
    public CompletionStage<FlowingResponse> handle(FlowingRequest request) {
        FlowingRequest inputPiped = pipeInput(request);
        if (inputPiped == null) {
            return null;
        }
        return getHandler().handle(inputPiped)
                .thenApplyAsync(this::pipeOutput);
    }

    private FlowingRequest pipeInput(FlowingRequest request) {
        FlowingRequest piping = request;
        for (InputPipe pipe : getInputPipes()) {
            piping = pipe.pipeIn(piping);
            if (piping == null) {
                LOGGER.debug("{}: overflowed in pipe {}", request, pipe);
                return null;
            }
        }
        return piping;
    }

    private FlowingResponse pipeOutput(FlowingResponse response) {
        for (OutputPipe pipe : getOutputPipes()) {
            response = pipe.pipeOut(response);
        }
        return response;
    }

}
