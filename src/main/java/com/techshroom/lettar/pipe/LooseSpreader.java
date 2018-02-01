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

import static com.techshroom.lettar.reflect.MethodHandles2.safeFindStatic;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.filterArguments;
import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;

/**
 * Method handle transform for *loose spreading* -- the first N elements of the
 * array are used, an exact match is not needed.
 */
public class LooseSpreader {

    public static MethodHandle asLooseSpreader(MethodHandle target, int pos) {
        int count = target.type().parameterCount() - pos;
        if (count == 0) {
            return dropArguments(target, pos, Object[].class);
        }
        MethodHandle spreader = target.asSpreader(Object[].class, count);
        MethodHandle shortenedByCopyOf = filterArguments(spreader, pos, copyOfFilter(count));
        return shortenedByCopyOf;
    }
    
    private static final MethodHandle ARRAYS_COPYOF = safeFindStatic(lookup(), Arrays.class, "copyOf", methodType(Object[].class, Object[].class, int.class));

    /**
     * Produce a method handle which takes an array and does
     * {@link Arrays#copyOf(Object[], int)}, with the second parameter bound to
     * {@code count}.
     * 
     * @param count
     * @return
     */
    private static MethodHandle copyOfFilter(int count) {
        return insertArguments(ARRAYS_COPYOF, 1, count);
    }

}
