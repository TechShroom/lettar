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
package com.techshroom.lettar.pipe;

import static com.google.common.base.Preconditions.checkState;
import static com.techshroom.lettar.reflect.MethodHandles2.invokeHandleUnchecked;
import static com.techshroom.lettar.reflect.MethodHandles2.safeUnreflect;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;
import com.techshroom.lettar.BaseRouterInitializer;
import com.techshroom.lettar.Request;
import com.techshroom.lettar.Response;
import com.techshroom.lettar.Router;
import com.techshroom.lettar.SimpleRequest;
import com.techshroom.lettar.annotation.NotFoundHandler;
import com.techshroom.lettar.annotation.ServerErrorHandler;
import com.techshroom.lettar.inheiritor.HashInheritorMap;
import com.techshroom.lettar.inheiritor.InheritorContext;
import com.techshroom.lettar.inheiritor.InheritorMap;
import com.techshroom.lettar.mime.MimeType;
import com.techshroom.lettar.pipe.PipelineRouter.Builder;
import com.techshroom.lettar.pipe.builtins.PathInputPipe;
import com.techshroom.lettar.pipe.builtins.accept.AcceptPipe;
import com.techshroom.lettar.pipe.impl.SimplePipeline;
import com.techshroom.lettar.util.Logging;

public class PipelineRouterInitializer extends BaseRouterInitializer<PipelineRouter.Builder> {

    private static final Logger LOGGER = Logging.getLogger();

    @Override
    protected <IB, OB> Router<IB, OB> newRouter(Builder carrier) {
        return carrier.build();
    }

    @Override
    protected Builder newCarrier() {
        return PipelineRouter.builder();
    }

    @Override
    protected void addController(Builder carrier, Object controller) {
        InheritanceHelper baseInheritance = new InheritanceHelper(HashInheritorMap.create())
                .inherit(controller.getClass());

        for (Method m : controller.getClass().getDeclaredMethods()) {
            if (!Modifier.isPublic(m.getModifiers()) || Modifier.isStatic(m.getModifiers())) {
                continue;
            }

            InheritanceHelper methodInheritance = baseInheritance.inherit(m);
            InheritorContext ctx = InheritorContext.from(m);

            Handler handler;
            Consumer<Pipeline> pipelineConsumer;
            if (m.isAnnotationPresent(ServerErrorHandler.class)) {
                handler = serverErrorHandler(controller, m);
                pipelineConsumer = carrier::serverErrorPipeline;
            } else if (m.isAnnotationPresent(NotFoundHandler.class)) {
                handler = notFoundHandler(controller, m);
                pipelineConsumer = carrier::notFoundPipeline;
            } else {
                handler = wrapMethod(controller, m);
                pipelineConsumer = carrier::addPipeline;
            }
            Pipeline pipe = pipeInheritorMap(methodInheritance.getInheritorMap(), handler, ctx);
            pipelineConsumer.accept(pipe);
        }
    }

    private Handler serverErrorHandler(Object controller, Method m) {
        MethodHandle base = safeUnreflect(m).bindTo(controller);
        MethodHandle call = PRIMethodHandles.errorHandlerTransform(base, m.getName());

        return flowReq -> {
            Request<Object> request = requestFromFlow(flowReq);
            Throwable err = flowReq.get(RequestKeys.error);

            CompletionStage<Response<Object>> response = invokeHandleUnchecked(() -> {
                return call.invoke(request, err);
            });

            return adaptResponse(response, flowReq);
        };
    }

    private Handler notFoundHandler(Object controller, Method m) {
        MethodHandle base = safeUnreflect(m).bindTo(controller);
        MethodHandle call = PRIMethodHandles.notFoundHandlerTransform(base, m.getName());

        return flowReq -> {
            Request<Object> request = requestFromFlow(flowReq);

            CompletionStage<Response<Object>> response = invokeHandleUnchecked(() -> {
                return call.invoke(request);
            });

            return adaptResponse(response, flowReq);
        };
    }

    private Handler wrapMethod(Object controller, Method handler) {
        // call(Request,MimeType,Object[])Response
        MethodHandle base = safeUnreflect(handler).bindTo(controller);
        MethodHandle call = PRIMethodHandles.routeTransform(base, handler.getName());

        return flowReq -> {
            Request<Object> request = requestFromFlow(flowReq);

            ImmutableList<String> pathParts = flowReq.get(PathInputPipe.parts);
            Object[] pathPartsArray = (pathParts == null ? ImmutableList.of() : pathParts).toArray();

            MimeType contentType = flowReq.get(AcceptPipe.contentType);
            checkState(contentType != null, "oh no, this shouldn't happen!");

            CompletionStage<Response<Object>> response = invokeHandleUnchecked(() -> {
                return call.invoke(request, contentType, pathPartsArray);
            });

            return adaptResponse(response, flowReq);
        };
    }

    private Request<Object> requestFromFlow(FlowingRequest flowReq) {
        Request<Object> request = SimpleRequest.builder()
                .body(flowReq.getBody())
                .headers(flowReq.getHeaders())
                .method(flowReq.getMethod())
                .path(String.join("/", flowReq.getPath()))
                .queryParts(flowReq.getQueryParts())
                .build();
        return request;
    }

    private static CompletionStage<FlowingResponse> adaptResponse(CompletionStage<Response<Object>> resStage, FlowingRequest request) {
        return resStage.thenApply(response -> BaseFlowingResponse.from(response.getStatusCode(), response.getBody(), response.getHeaders())
                .with(ResponseKeys.request, request));
    }

    private Pipeline pipeInheritorMap(InheritorMap map, Handler handler, InheritorContext ctx) {
        ImmutableList.Builder<InputPipe> inPipes = ImmutableList.builder();
        ImmutableList.Builder<OutputPipe> outPipes = ImmutableList.builder();
        for (InheritorMap.Entry entry : map) {
            Pipe pipe = entry.getInheritor().createPipe(entry.getOpaqueObject(), ctx);
            if (pipe instanceof InputPipe) {
                inPipes.add((InputPipe) pipe);
            }
            if (pipe instanceof OutputPipe) {
                outPipes.add((OutputPipe) pipe);
            }
        }
        ImmutableList<InputPipe> in = inPipes.build();
        ImmutableList<OutputPipe> out = outPipes.build();
        LOGGER.debug("New pipeline: in={}, out={}", in, out);
        return SimplePipeline.create(in, handler, out);
    }

}
