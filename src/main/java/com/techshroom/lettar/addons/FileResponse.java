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
package com.techshroom.lettar.addons;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.techshroom.lettar.Response;
import com.techshroom.lettar.SimpleResponse;
import com.techshroom.lettar.util.HttpUtil;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.mime.MediaType;

public class FileResponse {

    public static FileResponse fromDisk(Path path, Intent intent) throws IOException {
        return fromStream(Files.newInputStream(path), intent)
                .downloadedFileName(path.getFileName().toString())
                .length(Files.size(path));
    }

    public static FileResponse fromDisk(File file, Intent intent) throws IOException {
        return fromStream(new FileInputStream(file), intent)
                .downloadedFileName(file.getName())
                .length(file.length());
    }

    public static FileResponse fromStream(InputStream stream, Intent intent) {
        return new FileResponse(stream, intent);
    }

    public static final int UNKNOWN_LENGTH = -1;

    public enum Intent {

        DOWNLOAD(true, true),
        ASSET(false, false);

        private final boolean lengthImportant;
        private final boolean attachDisposition;

        Intent(boolean lengthImportant, boolean attachDisposition) {
            this.lengthImportant = lengthImportant;
            this.attachDisposition = attachDisposition;
        }

    }

    private InputStream stream;
    private Intent intent;
    private @Nullable String downloadedFileName;
    private long length = UNKNOWN_LENGTH;
    private MediaType contentType = MediaType.OCTET_STREAM;

    private FileResponse(InputStream stream, Intent intent) {
        this.stream = stream;
        this.intent = intent;
    }

    public InputStream getStream() {
        return stream;
    }

    public Intent getIntent() {
        return intent;
    }

    public FileResponse downloadedFileName(String downloadedFileName) {
        checkNotNull(downloadedFileName);
        this.downloadedFileName = downloadedFileName;
        return this;
    }

    @Nullable
    public String getDownloadedFileName() {
        return downloadedFileName;
    }

    public FileResponse length(long length) {
        checkArgument(length > 0 || length == UNKNOWN_LENGTH, "invalid length: %s", length);
        this.length = length;
        return this;
    }

    public long getLength() {
        return length;
    }

    public FileResponse contentType(MediaType contentType) {
        this.contentType = contentType;
        return this;
    }

    public MediaType getContentType() {
        return contentType;
    }

    public Response<? super InputStream> toStandardResponse() {
        ImmutableMap.Builder<String, String> headers = HttpUtil.singleHeaderMapBuilder();
        if (intent.attachDisposition) {
            checkNotNull(downloadedFileName, "File name required for intent %s", intent.name());
            headers.put(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + getDownloadedFileName() + "\"");
        }
        if (getLength() != UNKNOWN_LENGTH) {
            headers.put(HttpHeaders.CONTENT_LENGTH, String.valueOf(getLength()));
            if (intent.lengthImportant) {
                // inform further transformations that this should be kept
                // un-encoded
                headers.put(HttpHeaders.CONTENT_ENCODING, "identity");
            }
        }
        headers.put(HttpHeaders.CONTENT_TYPE, getContentType().toString());
        return SimpleResponse.builder()
                .ok_200()
                .body(stream)
                .headers(headers.build())
                .build();
    }

}
