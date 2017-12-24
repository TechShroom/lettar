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

import static com.techshroom.lettar.reflect.MethodHandles2.invokeHandleUnchecked;

import java.lang.invoke.MethodHandle;

import com.google.common.collect.ImmutableList;
import com.techshroom.lettar.transform.RouteTransform;
import com.techshroom.lettar.transform.TransformChain;
import com.techshroom.lettar.transform.TransformChainImpl;

class SRChains {

    public static <IB, OB> TransformChain<IB, OB> newChain(RouteEnhancements enhancements, MethodHandle base) {
        ImmutableList.Builder<RouteTransform<?, ?>> transforms = ImmutableList.builder();
        transforms.addAll(enhancements.routeTransforms());
        transforms.add(baseCallTransform(base));
        return TransformChainImpl.create(transforms.build());
    }

    private static RouteTransform<?, ?> baseCallTransform(MethodHandle base) {
        return chain -> invokeHandleUnchecked(() -> base.invoke(chain.request()));
    }

}
