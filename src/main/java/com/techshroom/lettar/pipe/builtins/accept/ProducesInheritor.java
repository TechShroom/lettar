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
package com.techshroom.lettar.pipe.builtins.accept;

import static com.google.common.collect.ImmutableList.toImmutableList;

import java.util.Optional;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.techshroom.lettar.inheiritor.CombiningInheritor;
import com.techshroom.lettar.inheiritor.Inheritor;
import com.techshroom.lettar.inheiritor.InheritorContext;
import com.techshroom.lettar.inheiritor.Required;
import com.techshroom.lettar.mime.MimeType;
import com.techshroom.lettar.pipe.Pipe;

@AutoService(Inheritor.class)
@Required
public class ProducesInheritor extends CombiningInheritor<ProducesData, Produces> {

    private static final MimeType DEFAULT = MimeType.of("application", "octet-stream");

    @Override
    protected ProducesData interpretAnnotation(Produces annotation) {
        return ProducesData.from(annotation);
    }

    @Override
    public Pipe createPipe(ImmutableList<ProducesData> data, InheritorContext ctx) {
        ImmutableList<MimeType> mimeTypes = data.stream()
                .map(ProducesData::getMimeType)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toImmutableList());
        MimeType defaultType = null;
        if (data.isEmpty() || Iterables.any(data, ProducesData::isMatchAnything)) {
            defaultType = Iterables.getFirst(mimeTypes, DEFAULT);
        }
        return AcceptPipe.create(mimeTypes, defaultType);
    }

}
