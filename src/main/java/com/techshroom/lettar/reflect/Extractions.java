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
package com.techshroom.lettar.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import com.techshroom.lettar.Request;

public class Extractions {

    public static Type getBodyTypeForMethod(Method method) {
        // method may not have request argument
        Type[] params = method.getGenericParameterTypes();
        if (params.length < 1) {
            return Object.class;
        }
        Type arg1 = params[0];
        if (!(arg1 instanceof ParameterizedType)) {
            // raw type or something weirder -- still just object!
            return Object.class;
        }
        ParameterizedType paramType = (ParameterizedType) arg1;
        if (paramType.getRawType() != Request.class) {
            return Object.class;
        }
        Type[] typeArgs = paramType.getActualTypeArguments();
        if (typeArgs.length < 1) {
            // raw type or something weirder -- still just object!
            return Object.class;
        }
        return resolveConcreteType(typeArgs[0]);
    }

    private static Type resolveConcreteType(Type type) {
        if (type instanceof WildcardType) {
            // reduce to upper/lower bound
            // this isn't perfect, but devs SHOULDNT be using wildcards...
            WildcardType wild = (WildcardType) type;
            if (wild.getLowerBounds().length > 0) {
                return wild.getLowerBounds()[0];
            }
            if (wild.getUpperBounds().length > 0) {
                return wild.getUpperBounds()[0];
            }
            return Object.class;
        }
        return type;
    }

}
