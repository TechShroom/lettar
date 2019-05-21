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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.techshroom.lettar.Request;
import com.techshroom.lettar.Response;
import com.techshroom.lettar.Router;
import com.techshroom.lettar.SimpleResponse;
import com.techshroom.lettar.collections.HttpMultimap;
import com.techshroom.lettar.util.Logging;

public class PipelineRouter<IB, OB> implements Router<IB, OB> {

    private static final Logger LOGGER = Logging.getLogger();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final ImmutableList.Builder<Pipeline> pipelines = ImmutableList.builder();
        private Pipeline notFoundPipeline;
        private Pipeline serverErrorPipeline;

        private Builder() {
        }

        public Builder notFoundPipeline(Pipeline notFoundPipeline) {
            this.notFoundPipeline = notFoundPipeline;
            return this;
        }

        public Builder serverErrorPipeline(Pipeline serverErrorPipeline) {
            this.serverErrorPipeline = serverErrorPipeline;
            return this;
        }

        public Builder addPipeline(Pipeline pipeline) {
            pipelines.add(pipeline);
            return this;
        }

        public <IB, OB> PipelineRouter<IB, OB> build() {
            checkNotNull(notFoundPipeline, "A NotFoundHandler is required.");
            checkNotNull(serverErrorPipeline, "A ServerErrorHandler is required.");
            return new PipelineRouter<>(pipelines.build(), notFoundPipeline, serverErrorPipeline);
        }

    }

    private final List<Pipeline> pipelines;
    private final Pipeline notFoundPipeline;
    private final Pipeline serverErrorPipeline;

    private PipelineRouter(List<Pipeline> pipelines, Pipeline notFoundPipeline, Pipeline serverErrorPipeline) {
        this.pipelines = pipelines;
        this.notFoundPipeline = notFoundPipeline;
        this.serverErrorPipeline = serverErrorPipeline;
    }

    @Override
    public CompletionStage<Response<OB>> route(Request<IB> request) {
        FlowingRequest flow = BaseFlowingRequest.wrap(request);
        CompletionStage<FlowingResponse> resStage = pipelines.stream()
                .map(p -> executePipeline(p, flow))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> executePipeline(notFoundPipeline, flow));
        if (resStage == null) {
            // NFP overflowed, this is not allowed!
            resStage = handleStateError(flow, "Not Found Pipeline overflow detected.", null);
        }
        return resStage
                .thenCompose(response -> {
                    return CompletableFuture.completedFuture(response);
                })
                .thenApply(this::extractResponse);
    }

    @Nullable
    private CompletionStage<FlowingResponse> executePipeline(Pipeline pipeline, FlowingRequest flow) {
        try {
            CompletionStage<FlowingResponse> resStage = pipeline.handle(flow);

            if (resStage == null) {
                return null;
            }

            CompletableFuture<FlowingResponse> ret = new CompletableFuture<>();
            resStage.whenComplete((val, ex) -> pipelineCompletion(flow, ret, val, ex));
            return ret;
        } catch (Throwable t) {
            return handleError(flow, t, flow.get(RequestKeys.error) == null);
        }
    }

    private void pipelineCompletion(FlowingRequest flow, CompletableFuture<FlowingResponse> ret, FlowingResponse val, Throwable ex) {
        if (ex != null) {
            handleError(flow, ex, flow.get(RequestKeys.error) == null)
                    .whenComplete((v, e) -> pipelineCompletion(flow, ret, v, e));
        } else {
            ret.complete(val);
        }
    }

    private Response<OB> extractResponse(FlowingResponse response) {
        return SimpleResponse.<OB> builder()
                .statusCode(response.getStatusCode())
                .body(response.getBody())
                .headers(response.getHeaders())
                .build();
    }

    private CompletionStage<FlowingResponse> handleStateError(FlowingRequest request, String error, Throwable cause) {
        return handleError(request, new IllegalStateException(error, cause), false);
    }

    private static final String OH_NO_BODY = "A pipeline leaked while handling another error. This is very bad. Contact the nearest developer immediately.\n";
    private static final HttpMultimap OH_NO_HEADERS = HttpMultimap.copyOfSingle(
            ImmutableMap.<String, String> builder()
                    .put("X-BadCode", "true")
                    .put("X-HitchhikerCount", "42")
                    .put("X-Quest", "Seek the Holy Grail")
                    .put("X-IsThisCodeTryingTooHardToBeFunny", "true")
                    .put("Content-length", String.valueOf(OH_NO_BODY.length()))
                    .put("Content-type", "text/plain")
                    .build());

    private CompletionStage<FlowingResponse> handleError(FlowingRequest request, Throwable t, boolean firstError) {
        if (!firstError) {
            LOGGER.error("Bad pipeline state detected", t);
            return CompletableFuture.completedFuture(BaseFlowingResponse.from(500,
                    OH_NO_BODY,
                    OH_NO_HEADERS));
        }
        FlowingRequest reqWithErr = request.with(RequestKeys.error, t);
        CompletionStage<FlowingResponse> response = executePipeline(serverErrorPipeline, reqWithErr);

        if (response == null) {
            return handleStateError(request, "Server Error Pipeline overflow detected.", t);
        }
        return response;
    }

}
