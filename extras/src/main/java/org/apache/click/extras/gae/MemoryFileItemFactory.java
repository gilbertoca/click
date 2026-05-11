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
package org.apache.click.extras.gae;

import org.apache.commons.fileupload2.core.FileItemFactory;

/**
 * Provides a FileItemFactory implementation that creates {@link MemoryFileItem}
 * instances which always keep their content in memory.
 * Used for Google App Engine (GAE) where disk access is restricted.
 */
public class MemoryFileItemFactory implements FileItemFactory<MemoryFileItem> {

    @Override
    public MemoryFileItemBuilder fileItemBuilder() {
        return new MemoryFileItemBuilder();
    }

    /**
     * Builder for MemoryFileItem. Extends AbstractFileItemBuilder which
     * already holds all the necessary fields (fieldName, contentType,
     * isFormField, fileName, fileItemHeaders, etc.).
     */
    public static class MemoryFileItemBuilder
            extends FileItemFactory.AbstractFileItemBuilder<MemoryFileItem, MemoryFileItemBuilder> {

        @Override
        public MemoryFileItem get() {
            return new MemoryFileItem(
                getFieldName(),
                getContentType(),   // já disponível diretamente no builder
                isFormField(),
                getFileName()
            );
        }
    }
}
