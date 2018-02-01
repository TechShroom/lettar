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
package com.techshroom.lettar;

import com.google.common.base.Throwables;
import com.techshroom.lettar.annotation.NotFoundHandler;
import com.techshroom.lettar.annotation.ServerErrorHandler;
import com.techshroom.lettar.pipe.builtins.path.Path;
import com.techshroom.lettar.pipe.builtins.query.Query;

public class TestRoutes {

    @Path("/")
    public Response<String> index() {
        return SimpleResponse.of(200, "Index Page");
    }

    @Path("/query")
    @Query("page=index")
    public Response<String> queryIndex() {
        return SimpleResponse.of(200, "Queried 'index' Page");
    }

    @Path("/query")
    @Query("page=action")
    public Response<String> queryAction() {
        return SimpleResponse.of(200, "Queried 'action' Page");
    }

    @Path("/{*}/list")
    public Response<String> resouceList(String resourceType) {
        return SimpleResponse.of(200, "[a,b,c," + resourceType + "]");
    }

    @Path("/ec/{**}")
    public Response<String> endChomp(String chomped) {
        return SimpleResponse.of(200, "CHOMP! " + chomped);
    }

    @Path("/re/{re:\\d+}")
    public Response<String> endChomp(int number) {
        return SimpleResponse.of(200, "RE: " + number);
    }

    @Path("/error")
    public Response<String> getError() {
        throw new AssertionError("Error Here");
    }

    @Path("/json")
    @TestBodyCodec
    public Response<String> json() {
        return SimpleResponse.of(200, "This Content Doesn't Matter!");
    }

    @ServerErrorHandler
    public Response<String> error(Throwable error) {
        if (error instanceof AssertionError) {
            return SimpleResponse.of(500, error.getMessage());
        }
        return SimpleResponse.of(500, "Error encountered: " + Throwables.getStackTraceAsString(error));
    }

    @NotFoundHandler
    public Response<String> notFound() {
        return SimpleResponse.of(404, "404 Page");
    }

}
