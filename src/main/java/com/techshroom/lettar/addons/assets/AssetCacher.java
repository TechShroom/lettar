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
package com.techshroom.lettar.addons.assets;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

class AssetCacher {

    private static final Splitter DOT = Splitter.on('.');

    private static String assetKey(String key) {
        Iterable<String> dotSplit = DOT.split(Paths.get(key).getFileName().toString());
        String ext = Iterables.getLast(
            StreamSupport.stream(dotSplit.spliterator(), false)
                .map(e -> "." + e).collect(Collectors.toList()),
            "");
        byte[] hash = Hashing.sha512().hashString(key, StandardCharsets.UTF_8).asBytes();
        return Base64.getUrlEncoder().encodeToString(hash) + ext;
    }

    private final Path cacheDirectory;
    {
        try {
            this.cacheDirectory = Files.createTempDirectory("lettar-asset-cache");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    private final Set<String> cached = Sets.newConcurrentHashSet();
    private final Lock lock = new ReentrantLock();

    public Path cache(InputStream resource, String assetKey) throws IOException {
        String key = assetKey(assetKey);
        if (!cached.contains(key)) {
            lock.lock();
            try {
                if (!cached.contains(key)) {
                    storeInCache(resource, key);
                    cached.add(key);
                }
            } finally {
                lock.unlock();
            }
        }
        return cacheDirectory.resolve(key);
    }

    private void storeInCache(InputStream resource, String key) throws IOException {
        Path location = cacheDirectory.resolve(key);
        try (InputStream input = resource;
                OutputStream output = new BufferedOutputStream(Files.newOutputStream(location))) {
            ByteStreams.copy(input, output);
        }
    }

}
