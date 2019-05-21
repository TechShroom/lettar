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
import com.techshroom.lettar.util.Logging;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.slf4j.Logger;

import java.io.BufferedInputStream;
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

    private static final Logger LOGGER = Logging.getLogger();

    public static AssetManager create(AssetLookup lookup) {
        return create(lookup, new DefaultDetector());
    }

    public static AssetManager create(AssetLookup lookup, Detector detector) {
        return new AssetManager(lookup, detector);
    }

    private final AssetCacher cacher = new AssetCacher();
    private final AssetLookup lookup;
    private final Detector detector;

    private AssetManager(AssetLookup lookup, Detector detector) {
        this.lookup = lookup;
        this.detector = detector;
    }

    public Response<? super InputStream> getAsset(String path) throws IOException {
        Asset asset = lookup.lookup(path);
        if (asset == null) {
            return SimpleResponse.of(404, path);
        }
        Path file = cacher.cache(asset.getStream(), path);
        String fileName = Paths.get(path).getFileName().toString();
        return FileResponse.fromDisk(file, Intent.ASSET)
            .downloadedFileName(fileName)
            .contentType(getContentType(file, file.getFileName().toString()))
            .toStandardResponse();
    }

    private MediaType getContentType(Path resource, String name) {
        Metadata metadata = new Metadata();
        metadata.add(Metadata.RESOURCE_NAME_KEY, name);
        try (InputStream stream = new BufferedInputStream(Files.newInputStream(resource))) {
            return detector.detect(stream, metadata);
        } catch (IOException e) {
            LOGGER.warn("Returning octet-stream for unknown file type due to I/O error", e);
            return MediaType.OCTET_STREAM;
        }
    }

}
