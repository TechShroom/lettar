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

import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.techshroom.lettar.Logging;
import com.techshroom.lettar.Request;
import com.techshroom.lettar.Response;
import com.techshroom.lettar.Router;
import com.techshroom.lettar.SimpleResponse;
import com.techshroom.lettar.collections.HttpMultimap;

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
    public Response<OB> route(Request<IB> request) {
        FlowingRequest flow = BaseFlowingRequest.wrap(request);
        FlowingResponse response = pipelines.stream()
                .map(p -> executePipeline(p, flow))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> executePipeline(notFoundPipeline, flow));
        if (response == null) {
            // NFP overflowed, this is not allowed!
            response = handleStateError(flow, "Not Found Pipeline overflow detected.", null);
        }
        return extractResponse(response);
    }

    private FlowingResponse executePipeline(Pipeline pipeline, FlowingRequest flow) {
        try {
            return pipeline.handle(flow);
        } catch (Throwable t) {
            return handleError(flow, t, flow.get(RequestKeys.error) == null);
        }
    }

    private Response<OB> extractResponse(FlowingResponse response) {
        return SimpleResponse.<OB> builder()
                .statusCode(response.getStatusCode())
                .body(response.getBody())
                .headers(response.getHeaders())
                .build();
    }

    private FlowingResponse handleStateError(FlowingRequest request, String error, Throwable cause) {
        return handleError(request, new IllegalStateException(error, cause), false);
    }

    private static final String OH_NO_BODY = "A pipeline leaked while handling another error. This is very bad. Contact the nearest developer immediately.\n";
    private static final HttpMultimap OH_NO_HEADERS = HttpMultimap.copyOf(
            ImmutableMap.<String, String> builder()
                    .put("X-BadCode", "true")
                    .put("X-HitchhikerCount", "42")
                    .put("X-Quest", "Seek the Holy Grail")
                    .put("X-IsThisCodeTryingTooHardToBeFunny", "true")
                    .put("Content-length", String.valueOf(OH_NO_BODY.length()))
                    .put("Content-type", "text/plain")
                    .build());

    private FlowingResponse handleError(FlowingRequest request, Throwable t, boolean firstError) {
        if (!firstError) {
            LOGGER.error("Bad pipeline state detected", t);
            return BaseFlowingResponse.from(500,
                    OH_NO_BODY,
                    OH_NO_HEADERS);
        }
        FlowingRequest reqWithErr = request.with(RequestKeys.error, t);
        FlowingResponse response = executePipeline(serverErrorPipeline, reqWithErr);
        if (response == null) {
            return handleStateError(request, "Server Error Pipeline overflow detected.", t);
        }
        return response;
    }

}
