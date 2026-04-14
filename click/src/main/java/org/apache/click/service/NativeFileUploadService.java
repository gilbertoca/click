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
package org.apache.click.service;

import java.util.Collection;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import org.apache.click.util.ClickUtils;
import org.apache.commons.lang3.Validate;

/**
 * Provides a native Servlet 3.0+ FileUploadService implementation.
 * This service replaces the dependency on Apache Commons FileUpload.
 */
public class NativeFileUploadService implements FileUploadService {

    /** The total request maximum size in bytes. */
    protected long sizeMax = -1L;

    /** The maximum individual file size in bytes. */
    protected long fileSizeMax = -1L;

    @Override
    public void onInit(ServletContext servletContext) throws Exception {
        ConfigService configService = ClickUtils.getConfigService(servletContext);
        LogService logService = configService.getLogService();
        
        // Note: With Servlet 3.0+, limits are usually enforced by the container 
        // via @MultipartConfig or web.xml. We keep these fields for Click 
        // configuration compatibility, but they must be supported by the container.
        logService.info("NativeFileUploadService initialized using native Servlet container multipart support.");
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public Collection<Part> parseRequest(HttpServletRequest request) throws ServletException {
        Validate.notNull(request, "Null request parameter");

        try {
            return request.getParts();
        } catch (java.io.IOException ioe) {
            throw new ServletException("IO error parsing multipart request", ioe);
        } catch (IllegalStateException ise) {
            // This occurs if the servlet is not configured with <multipart-config>
            throw new ServletException("Servlet is not configured to handle multipart requests. " 
                + "Ensure @MultipartConfig is added to ClickServlet or defined in web.xml", ise);
        }
    }

    // Keep getters/setters for Click's property injection (click.xml)
    public long getFileSizeMax() { return fileSizeMax; }
    public void setFileSizeMax(long value) { this.fileSizeMax = value; }
    public long getSizeMax() { return sizeMax; }
    public void setSizeMax(long value) { this.sizeMax = value; }
}

