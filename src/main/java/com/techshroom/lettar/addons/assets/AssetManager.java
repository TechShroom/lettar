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

import com.techshroom.lettar.Response;
import com.techshroom.lettar.SimpleResponse;
import com.techshroom.lettar.addons.FileResponse;
import com.techshroom.lettar.addons.FileResponse.Intent;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Sub-server for managing assets. Add a route with a {@code Path} rule with the
 * desired sub-path followed by {@code **}. For example, if all assets should be
 * served from {@code /assets/}, then a route would be annotated like this:
 * {@code @Path("/assets/{**}")}. Then, pass the captured path to the manager
 * and return the response.
 */
public class AssetManager {

    public static AssetManager create(AssetLookup lookup) {
        return new AssetManager(lookup);
    }

    private final AssetCacher cacher = new AssetCacher();
    private final AssetLookup lookup;

    private AssetManager(AssetLookup lookup) {
        this.lookup = lookup;
    }

    public Response<? super InputStream> getAsset(String path) throws IOException {
        Asset asset = lookup.lookup(path);
        if (asset == null) {
            return SimpleResponse.of(404, path);
        }
        Path file = cacher.cache(asset.getStream(), path);
        return FileResponse.fromDisk(file, Intent.ASSET)
                .downloadedFileName(Paths.get(path).getFileName().toString())
                .toStandardResponse();
    }

}
