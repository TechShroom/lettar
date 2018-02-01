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

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.filterArguments;
import static java.lang.invoke.MethodHandles.insertArguments;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import com.techshroom.lettar.Request;
import com.techshroom.lettar.Response;
import com.techshroom.lettar.mime.MimeType;
import com.techshroom.lettar.reflect.StringConverters;

class PRIMethodHandles {

    private static void verifyResponseReturn(MethodType type, String name) {
        checkArgument(Response.class.isAssignableFrom(type.returnType()),
                "response return required (method %s%s)", name, type);
    }

    public static MethodHandle errorHandlerTransform(MethodHandle base, String name) {
        // need (LRequest;LException;)LResponse
        MethodType originalType = base.type();

        base = injectParameter(base, 0, Request.class);
        base = injectParameter(base, 1, Throwable.class);

        checkArgument(base.type().parameterCount() == 2,
                "Only a Request and Throwable are allowed as parameters (method %s%s)",
                name, originalType);

        verifyResponseReturn(base.type(), name);
        return base;
    }

    public static MethodHandle notFoundHandlerTransform(MethodHandle base, String name) {
        // need (LRequest;)LResponse;
        MethodType originalType = base.type();

        base = injectParameter(base, 0, Request.class);

        checkArgument(base.type().parameterCount() == 1,
                "Only a Request is allowed as a parameter (method %s%s)",
                name, originalType);

        verifyResponseReturn(base.type(), name);
        return base;
    }

    public static MethodHandle routeTransform(MethodHandle base, String name) {
        // need (LRequest;LMimeType;[LObject;)LResponse;

        base = injectParameter(base, 0, Request.class);
        base = injectParameter(base, 1, MimeType.class);

        MethodType type = base.type();

        // transform parameters as needed -- we want all inputs to be String
        MethodHandle[] filters = new MethodHandle[type.parameterCount()];
        for (int i = 2; i < type.parameterCount(); i++) {
            Class<?> parameter = type.parameterType(i);
            filters[i] = StringConverters.getHandleForArgType(parameter);
        }
        base = filterArguments(base, 0, filters);

        // mix into Object[] loosely
        base = LooseSpreader.asLooseSpreader(base, 2);

        verifyResponseReturn(base.type(), name);
        return base;
    }

    public static MethodHandle fillThisParam(MethodHandle base, Object $this) {
        if ($this == null) {
            return base;
        }
        return insertArguments(base, 0, $this);
    }

    public static MethodHandle injectParameter(MethodHandle base, int index, Class<?> paramType) {
        MethodType type = base.type();

        // first, check $index parameter, inject type if needed
        int params = Math.max(type.parameterCount() - index + 1, 0);
        switch (params) {
            case 0:
                // invalid state - we should already have params up to index
                throw new IllegalStateException("Missing " + index + " parameter(s).");
            case 1:
                // no parameter - inject as a fake parameter
                base = dropArguments(base, index, paramType);
                break;
            default:
                // perhaps inject
                if (type.parameterType(index).isAssignableFrom(paramType)) {
                    // all good
                    break;
                }
                base = dropArguments(base, index, paramType);
        }
        return base;
    }

}
