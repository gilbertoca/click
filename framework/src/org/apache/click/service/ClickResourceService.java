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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.servlet.ServletContext;

import org.apache.click.util.ClickUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 * Provides a default Click static resource service class.
 * <p/>
 * TODO
 */
public class ClickResourceService implements ResourceService {

    /** The click resources cache. */
    protected Map<String, byte[]> resourceCache = new HashMap<String, byte[]>();

    /** The application log service. */
    protected LogService logService;

    /**
     * @see ResourceService#onInit(ServletContext)
     *
     * @param servletContext the application servlet context
     * @throws IOException if an IO error occurs initializing the service
     */
    public void onInit(ServletContext servletContext) throws IOException {

        ConfigService configService = ClickUtils.getConfigService(servletContext);
        logService = configService.getLogService();

        // Load all JAR resources
        loadAllJarResources();

        // Load all file system resources
        loadClickDirResources(servletContext);
    }

    /**
     * @see ResourceService#onDestroy()
     */
    public void onDestroy() {
        resourceCache.clear();
    }

    /**
     * @see ResourceService#getResourceData(String)
     *
     * @param resourcePath the path of the resource to lookup
     * @return the resource data byte array if found or null otherwise
     */
    public byte[] getResourceData(String resourcePath) {
        return resourceCache.get(resourcePath);
    }

    // Private Methods --------------------------------------------------------

    private void loadAllJarResources() throws IOException {

        // Find all jars under WEB-INF/lib and deploy all resources from these jars
        long startTime = System.currentTimeMillis();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        Enumeration<URL> en = classLoader.getResources("META-INF/web");
        while (en.hasMoreElements()) {
            URL url = en.nextElement();
            String path = url.getFile();

            // Decode the url, esp on Windows where file paths can have their
            // spaces encoded. decodeURL will convert C:\Program%20Files\project
            // to C:\Program Files\project
            path = ClickUtils.decodeURL(path);

            // Strip file prefix
            if (path.startsWith("file:")) {
                path = path.substring(5);
            }

            String jarPath = null;

            // Check if path represents a jar
            if (path.indexOf('!') > 0) {
                jarPath = path.substring(0, path.indexOf('!'));

                File jar = new File(jarPath);

                if (jar.exists()) {
                    loadFilesInJar(jar);

                } else {
                    logService.error("Could not load the jar '" + jarPath
                        + "'. Please ensure this file exists in the specified"
                        + " location.");
                }
            } else {
                File dir = new File(path);
                loadFilesInJarDir(dir);
            }
        }

        if (logService.isTraceEnabled()) {
            logService.trace("loaded files from jars and folders - "
                + (System.currentTimeMillis() - startTime) + " ms");
        }
    }

    private void loadFilesInJar(File jar) throws IOException {
        if (jar == null) {
            throw new IllegalArgumentException("Jar cannot be null");
        }

        InputStream inputStream = null;
        JarInputStream jarInputStream = null;

        try {

            inputStream = new FileInputStream(jar);
            jarInputStream = new JarInputStream(inputStream);
            JarEntry jarEntry = null;

            // Indicates whether feedback should be logged about the files deployed
            // from jar
            boolean logFeedback = true;
            while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {

                // Guard against loading folders -> META-INF/web/click/
                if (jarEntry.isDirectory()) {
                    continue;
                }

                // jarEntryName example -> META-INF/web/click/table.css
                String jarEntryName = jarEntry.getName();

                // Only deploy resources from "META-INF/web/"
                int pathIndex = jarEntryName.indexOf("META-INF/web/");
                if (pathIndex == 0) {
                    if (logFeedback && logService.isTraceEnabled()) {
                        logService.trace("loaded files from jar -> "
                                         + jar.getCanonicalPath());

                        // Only provide feedback once per jar
                        logFeedback = false;
                    }
                    loadJarFile(jarEntryName, "META-INF/web/");
                }
            }
        } finally {
            ClickUtils.close(jarInputStream);
            ClickUtils.close(inputStream);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFilesInJarDir(File dir) throws IOException {
        if (dir == null) {
            throw new IllegalArgumentException("Dir cannot be null");
        }

        if (!dir.exists()) {
            logService.trace("There are no files in the folder '"
                + dir.getAbsolutePath() + "'");
            return;
        }

        Iterator files = FileUtils.iterateFiles(dir,
                                                TrueFileFilter.INSTANCE,
                                                TrueFileFilter.INSTANCE);

        boolean logFeedback = true;
        while (files.hasNext()) {
            // file example -> META-INF/web/click/table.css
            File file = (File) files.next();

            // Guard against loading folders -> META-INF/web/click/
            if (file.isDirectory()) {
                continue;
            }

            String fileName = file.getCanonicalPath().replace('\\', '/');

            // Only deploy resources from "META-INF/web/"
            int pathIndex = fileName.indexOf("META-INF/web/");
            if (pathIndex != -1) {
                if (logFeedback && logService.isTraceEnabled()) {
                    logService.trace("load files from folder -> "
                        + dir.getAbsolutePath());

                    // Only provide feedback once per dir
                    logFeedback = false;
                }
                fileName = fileName.substring(pathIndex);
                loadJarFile(fileName, "META-INF/web/");
            }
        }
    }

    private void loadJarFile(String file, String prefix) throws IOException {
        // Only deploy resources containing the prefix
        int pathIndex = file.indexOf(prefix);
        if (pathIndex == 0) {
            pathIndex += prefix.length();

            // resourceName example -> click/table.css
            String resourceName = file.substring(pathIndex);

            if (resourceName.length() > 0) {
                byte[] resourceBytes = getResourceByteArray(file);

                resourceCache.put("/" + resourceName, resourceBytes);
            }
        }
    }

    private byte[] getResourceByteArray(String name)
        throws IOException {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        InputStream inputStream = classLoader.getResourceAsStream(name);
        if (inputStream == null) {
            inputStream = getClass().getResourceAsStream(name);
        }

        try {
            byte[] resourceData = IOUtils.toByteArray(inputStream);

            return resourceData;

        } finally {
            ClickUtils.close(inputStream);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadClickDirResources(ServletContext servletContext)
        throws IOException {

        Set resources = servletContext.getResourcePaths("/click/");

        if (resources != null) {
            // Add all resources withtin web application
            for (Iterator i = resources.iterator(); i.hasNext();) {
                String resource = (String) i.next();

                if (!resource.endsWith(".htm") && !resource.endsWith(".jsp")
                    && !resource.endsWith("/")) {

                    InputStream inputStream = null;
                    try {
                        inputStream = servletContext.getResourceAsStream(
                            resource);

                        byte[] resourceData = IOUtils.toByteArray(inputStream);

                        resourceCache.put(resource, resourceData);

                    } finally {
                        ClickUtils.close(inputStream);
                    }
                }
            }
        }
    }

}
