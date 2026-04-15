/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.click.fileupload;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;

import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.FileItemHeaders;

/**
 * Mock implementation of org.apache.commons.fileupload2.core.FileItem.
 */
public class MockFileItem implements FileItem<MockFileItem> {

    private static final long serialVersionUID = 1L;

    @Override
    public MockFileItem delete() throws IOException {
        return this;
    }

    @Override
    public byte[] get() {
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public String getFieldName() {
        return null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public String getString() {
        return null;
    }

    @Override
    public String getString(Charset charset) throws IOException {
        return null;
    }

    @Override
    public boolean isFormField() {
        return false;
    }

    @Override
    public boolean isInMemory() {
        return false;
    }

    @Override
    public MockFileItem setFieldName(String arg0) {
        return this;
    }

    @Override
    public MockFileItem setFormField(boolean arg0) {
        return this;
    }

    @Override
    public MockFileItem write(Path path) throws IOException {
        return this;
    }

    @Override
    public FileItemHeaders getHeaders() {
        return null;
    }

    @Override
    public MockFileItem setHeaders(FileItemHeaders fileItemHeaders) {
        return this;
    }

    // --- New methods in 2.x to satisfy the interface ---

    public Charset getCharset() {
        return null;
    }

    public MockFileItem setCharset(Charset charset) {
        return this;
    }
}

