/*
 * This file is part of Lettar, licensed under the MIT License (MIT).
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
import com.techshroom.lettar.annotation.Route;
import com.techshroom.lettar.annotation.ServerErrorHandler;

public class TestRoutes {

    @Route(path = "/")
    public Response<String> index() {
        return SimpleResponse.of(200, "Index Page");
    }

    @Route(path = "/query", params = "page=index")
    public Response<String> queryIndex() {
        return SimpleResponse.of(200, "Queried 'index' Page");
    }

    @Route(path = "/query", params = "page=action")
    public Response<String> queryAction() {
        return SimpleResponse.of(200, "Queried 'action' Page");
    }

    @Route(path = "/{*}/list")
    public Response<String> resouceList(String resourceType) {
        return SimpleResponse.of(200, "[a,b,c," + resourceType + "]");
    }

    @Route(path = "/ec/{**}")
    public Response<String> endChomp(String chomped) {
        return SimpleResponse.of(200, "CHOMP! " + chomped);
    }

    @Route(path = "/re/{re:\\d+}")
    public Response<String> endChomp(int number) {
        return SimpleResponse.of(200, "RE: " + number);
    }

    @Route(path = "/error")
    public Response<String> getError() {
        throw new AssertionError("Error Here");
    }

    @ServerErrorHandler
    public Response<String> error(Exception error) {
        return SimpleResponse.of(500, "Error encountered: " + Throwables.getStackTraceAsString(error));
    }

    @ServerErrorHandler(exception = AssertionError.class)
    public Response<String> assertionFailed(AssertionError error) {
        return SimpleResponse.of(500, "Error encountered: " + error.getMessage());
    }

    @NotFoundHandler
    public Response<String> notFound() {
        return SimpleResponse.of(404, "404 Page");
    }

}
