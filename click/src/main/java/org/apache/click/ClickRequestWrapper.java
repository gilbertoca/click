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
package org.apache.click;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.Part;

import org.apache.click.service.FileUploadService;
import org.apache.click.util.ClickUtils;
import org.apache.commons.io.IOUtils;

/**
 * Provides a custom HttpServletRequest class for shielding users from
 * multipart request parameters. Thus calling request.getParameter(String)
 * will still work properly.
 */
class ClickRequestWrapper extends HttpServletRequestWrapper {

    /**
     * The <code>FileItem</code> objects for <code>"multipart"</code> POST requests.
     */
    private final Map<String, Part[]> fileItemMap;

    /** The request is a multi-part file upload POST request. */
    private final boolean isMultipartRequest;

    /** The map of <code>"multipart"</code> request parameter values. */
    private final Map<String, String[]> multipartParameterMap;

    /** The wrapped servlet request. */
    private final HttpServletRequest request;

    // Constructors -----------------------------------------------------------

    /**
     * @see HttpServletRequestWrapper(HttpServletRequest)
     */
    ClickRequestWrapper(final HttpServletRequest request,
        final FileUploadService fileUploadService) {
        super(request);

        this.isMultipartRequest = ClickUtils.isMultipartRequest(request);
        this.request = request;

        if (isMultipartRequest) {

            Map<String, String[]> requestParams = new HashMap<String, String[]>();
            Map<String, Part[]> fileItems = new HashMap<String, Part[]>();

            try {
                // Use native Servlet 3.0+ method
                Collection<Part> parts = request.getParts();
                
                for (Part part : parts) {
                    String name = part.getName();
                    
                    // Native check: form fields vs uploaded files
                    if (part.getSubmittedFileName() == null) {
                        // Handle as standard form field
                        String value = IOUtils.toString(part.getInputStream(), getCharacterEncoding());
                        addToMapAsString(requestParams, name, value);
                    } else {
                        // Handle as file upload
                        addToMapAsFileItem(fileItems, name, part);
                    }
                }
            }  catch (Throwable t) {

                // Don't throw error here as it will break Context creation.
                // Instead add the error as a request attribute.
                request.setAttribute(Context.CONTEXT_FATAL_ERROR, t);

            } finally {
                fileItemMap = Collections.unmodifiableMap(fileItems);
                multipartParameterMap = Collections.unmodifiableMap(requestParams);
            }

        } else {
            fileItemMap = Collections.emptyMap();
            multipartParameterMap = Collections.emptyMap();
        }
    }

    // Public Methods ---------------------------------------------------------

    /**
     * Returns a map of <code>FileItem arrays</code> keyed on request parameter
     * name for "multipart" POST requests (file uploads). Thus each map entry
     * will consist of one or more <code>FileItem</code> objects.
     *
     * @return map of <code>FileItem arrays</code> keyed on request parameter name
     * for "multipart" POST requests
     */
    public Map<String, Part[]> getFileItemMap() {
        return fileItemMap;
    }

    /**
     * @see javax.servlet.ServletRequest#getParameter(String)
     */
    @Override
    public String getParameter(String name) {
        if (isMultipartRequest) {
            Object value = getMultipartParameterMap().get(name);

            if (value instanceof String) {
                return (String) value;
            }

            if (value instanceof String[]) {
                String[] array = (String[]) value;
                if (array.length >= 1) {
                    return array[0];
                } else {
                    return null;
                }
            }

            return (value == null ? null : value.toString());

        } else {
            return request.getParameter(name);
        }
    }

    /**
     * @see javax.servlet.ServletRequest#getParameterNames()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Enumeration<String> getParameterNames() {
        if (isMultipartRequest) {
            return Collections.enumeration(getMultipartParameterMap().keySet());

        } else {
            return request.getParameterNames();
        }
    }

    /**
     * @see javax.servlet.ServletRequest#getParameterValues(String)
     */
    @Override
    public String[] getParameterValues(String name) {
        if (isMultipartRequest) {
            Object values = getMultipartParameterMap().get(name);
            if (values instanceof String) {
                return new String[] { values.toString() };
            }
            if (values instanceof String[]) {
                return (String[]) values;
            } else {
                return null;
            }

        } else {
            return request.getParameterValues(name);
        }
    }

    /**
     * @see javax.servlet.ServletRequest#getParameterMap()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String[]> getParameterMap() {
        if (isMultipartRequest) {
            return getMultipartParameterMap();
        } else {
            return request.getParameterMap();
        }
    }

    // Package Private Methods ------------------------------------------------

    /**
     * Return the map of <code>"multipart"</code> request parameter map.
     *
     * @return the <code>"multipart"</code> request parameter map
     */
    @SuppressWarnings("unchecked")
    Map<String, String[]> getMultipartParameterMap() {
        if (request.getAttribute(ClickServlet.MOCK_MODE_ENABLED) == null) {
            return multipartParameterMap;
        } else {
            // In mock mode return the request parameter map. This ensures
            // calling request.setParameter(x,y) works for both normal and
            // multipart requests.
            return request.getParameterMap();
        }
    }

    // Private Methods --------------------------------------------------------

    /**
     * Stores the specified value in a FileItem array in the map, under the
     * specified name. Thus two values stored under the same name will be
     * stored in the same array.
     *
     * @param map the map to add the specified name and value to
     * @param name the name of the map key
     * @param value the value to add to the FileItem array
     */
    private void addToMapAsFileItem(Map<String, Part[]> map, String name, Part value) {
        Part[] oldValues = map.get(name);
        Part[] newValues = null;
        if (oldValues == null) {
            newValues = new Part[] {value};
        } else {
            newValues = new Part[oldValues.length + 1];
            System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
            newValues[oldValues.length] = value;
        }
        map.put(name, newValues);
    }

    /**
     * Stores the specified value in an String array in the map, under the
     * specified name. Thus two values stored under the same name will be
     * stored in the same array.
     *
     * @param map the map to add the specified name and value to
     * @param name the name of the map key
     * @param value the value to add to the string array
     */
    private void addToMapAsString(Map<String, String[]> map, String name, String value) {
        String[] oldValues = map.get(name);
        String[] newValues = null;
        if (oldValues == null) {
            newValues = new String[] {value};
        } else {
            newValues = new String[oldValues.length + 1];
            System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
            newValues[oldValues.length] = value;
        }
        map.put(name, newValues);
    }

}
