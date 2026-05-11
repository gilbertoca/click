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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletContext;
import org.apache.click.Control;

import org.apache.click.util.ClickUtils;
import org.apache.click.util.HtmlStringBuffer;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Extends {@link BasicResourceService} to provide backward compatibility by
 * automatically deploying static resources to the physical "/click" directory.
 * <p/>
 * This service is the default implementation and is suitable for environments
 * where developers wish to override or customize Click's internal resources
 * directly on the file system.
 *
 * @since 2.5.0
 */
public class ClickResourceService extends BasicResourceService {

    
    /** The ResourceService root element. */
    protected Element configElement;        
    
    /**
     * @see ResourceService#onInit(ServletContext)
     *
     * @param servletContext the application servlet context
     * @throws IOException if an IO error occurs initializing the service
     */
    @Override
    public void onInit(ServletContext servletContext) throws IOException {

        super.onInit(servletContext);
        
        try {
              deployFiles(configElement);
        } catch (Exception e) {
              // Encapsula qualquer erro de Reflection ou XML em IOException
              if (e instanceof IOException) {
                  throw (IOException) e;
              }
              throw new IOException("Error deploying resources", e);
        }

    }

    public Element getConfigElement() {
        return configElement;
    }

    public void setConfigElement(Element configElement) {
        this.configElement = configElement;
    }

    
    // ------------------------------------------------ Package Private Methods
    
    /**
     * Returns true if Click resources (JavaScript, CSS, images etc) packaged
     * in jars can be deployed to the root directory of the webapp, false
     * otherwise.
     * <p>
     * By default this method will return false in restricted environments where
     * write access to the underlying file system is disallowed. Example
     * environments where write access is not allowed include the WebLogic JEE
     * server and Google App Engine. (Note: WebLogic provides the property
     * <code>"Archived Real Path Enabled"</code> that controls whether web
     * applications can access the file system or not. See the Click user manual
     * for details).
     *
     * @return true if resources can be deployed, false otherwise
     */
    private boolean isResourcesDeployable() {
        // Only deploy if writes are allowed
        return ClickUtils.isResourcesDeployable(configService.getServletContext());
    }
    
    private Element getResourceRootElement(String path) throws IOException {
        Document document = null;
        InputStream inputStream = null;
        try {
            inputStream = ClickUtils.getResourceAsStream(path, getClass());

            if (inputStream != null) {
                document = ClickUtils.buildDocument(inputStream, 
                        (configService instanceof org.xml.sax.EntityResolver) ? (org.xml.sax.EntityResolver) configService : null);
            }

        } finally {
            ClickUtils.close(inputStream);
        }

        if (document != null) {
            return document.getDocumentElement();

        } else {
            return null;
        }
    }

    private void deployControls(Element rootElm) throws Exception {

        if (rootElm == null) {
            return;
        }

        Element controlsElm = ClickUtils.getChild(rootElm, "controls");

        if (controlsElm == null) {
            return;
        }

        for (Element deployableElm : ClickUtils.getChildren(controlsElm, "control")) {

            String classname = deployableElm.getAttribute("classname");
            if (StringUtils.isBlank(classname)) {
                String msg =
                    "'control' element missing 'classname' attribute.";
                throw new RuntimeException(msg);
            }

            Class deployClass = ClickUtils.classForName(classname);
            Control control = (Control) deployClass.getDeclaredConstructor().newInstance();

            control.onDeploy(configService.getServletContext());
        }
    }

    private void deployControlSets(Element rootElm) throws Exception {
        if (rootElm == null) {
            return;
        }

        Element controlsElm = ClickUtils.getChild(rootElm, "controls");

        if (controlsElm == null) {
            return;
        }

        for (Element controlSet : ClickUtils.getChildren(controlsElm, "control-set")) {

            String name = controlSet.getAttribute("name");
            if (StringUtils.isBlank(name)) {
                String msg =
                        "'control-set' element missing 'name' attribute.";
                throw new RuntimeException(msg);
            }
            deployControls(getResourceRootElement("/" + name));
        }
    }

    /**
     * Deploy from the classpath all resources found under the directory
     * 'META-INF/resources/'. For backwards compatibility resources under the
     * directory 'META-INF/web/' are also deployed.
     * <p>
     * Only jars and folders available on the classpath are scanned.
     *
     * @throws IOException if the resources cannot be deployed
     */
    private void deployResourcesOnClasspath() throws IOException {
        long startTime = System.currentTimeMillis();

        // Find all jars and directories on the classpath that contains the
        // directory "META-INF/resources/", and deploy those resources
        String resourceDirectory = "META-INF/resources";

        List<String> resources = new DeployUtils(logService).findResources(resourceDirectory).getResources();
        for (String resource : resources) {
            deployFile(resource, resourceDirectory);
        }

        // For backward compatibility, find all jars and directories on the
        // classpath that contains the directory "META-INF/web/", and deploy those
        // resources
        resourceDirectory = "META-INF/web";
        resources = new DeployUtils(logService).findResources(resourceDirectory).getResources();
        for (String resource : resources) {
            deployFile(resource, resourceDirectory);
        }

        logService.trace("deployed files from jars and folders - "
            + (System.currentTimeMillis() - startTime) + " ms");
    }
    
    /**
     * Deploy the specified file.
     *
     * @param file the file to deploy
     * @param prefix the file prefix that must be removed when the file is
     * deployed
     */
    private void deployFile(String file, String prefix) {
        // Only deploy resources containing the prefix
        int pathIndex = file.indexOf(prefix);
        if (pathIndex == 0) {
            pathIndex += prefix.length();

            // By default deploy to the web root dir
            String targetDir = "";

            // resourceName example -> click/table.css
            String resourceName = file.substring(pathIndex);
            int index = resourceName.lastIndexOf('/');

            if (index != -1) {
                // targetDir example -> click
                targetDir = resourceName.substring(0, index);
            }

            // Copy resources to web folder
            ClickUtils.deployFile(configService.getServletContext(),
                                  file,
                                  targetDir);
        }
    }
    
    /**
     * Deploy files from jars and Controls.
     *
     * @param rootElm the click.xml configuration DOM element
     * @throws java.lang.Exception if files cannot be deployed
     */
    private void deployFiles(Element rootElm) throws Exception {

        boolean isResourcesDeployable = isResourcesDeployable();

        if (isResourcesDeployable) {
            if (logService.isTraceEnabled()) {
                String deployTarget = configService.getServletContext().getRealPath("/");
                logService.trace("resource deploy folder: "
                        + deployTarget);
            }

            deployControls(getResourceRootElement("/click-controls.xml"));
            deployControls(getResourceRootElement("/extras-controls.xml"));
            deployControls(rootElm);
            deployControlSets(rootElm);
            deployResourcesOnClasspath();
        }

        if (!isResourcesDeployable) {

            HtmlStringBuffer buffer = new HtmlStringBuffer();
            buffer.append("could not deploy Click resources to the 'click'");
            buffer.append(" web folder.\nThis can occur if the call to");
            buffer.append(" ServletContext.getRealPath(\"/\") returns null, which means");
            buffer.append(" the web application cannot determine the file system path");
            buffer.append(" to deploy files to. This issue also occurs if the web");
            buffer.append(" application is not allowed to write to the file");
            buffer.append(" system.\n");

            buffer.append("To resolve this issue please see the Click user-guide:");
            buffer.append(" http://click.apache.org/docs/user-guide/html/ch05s03.html#deploying-restricted-env");
            buffer.append(" \nIgnore this warning once you have settled on a");
            buffer.append(" deployment strategy");
            logService.warn(buffer.toString());
        }
    }

}
