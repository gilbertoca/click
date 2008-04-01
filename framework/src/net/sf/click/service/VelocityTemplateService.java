/*
 * Copyright 2008 Malcolm A. Edgar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.click.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.servlet.ServletContext;

import net.sf.click.Page;
import net.sf.click.util.ClickLogger;
import net.sf.click.util.ErrorReport;

import org.apache.commons.lang.Validate;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.io.VelocityWriter;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.tools.view.servlet.WebappLoader;
import org.apache.velocity.util.SimplePool;

/**
 * Provides a Velocity TemplateService class.
 *
 * @author Malcolm Edgar
 */
public class VelocityTemplateService implements TemplateService {

    // -------------------------------------------------------------- Constants

    /**
     * The default velocity properties filename: &nbsp;
     * "<tt>/WEB-INF/velocity.properties</tt>".
     */
    protected static final String DEFAULT_TEMPLATE_PROPS = "/WEB-INF/velocity.properties";

    /**
     * The user supplied macro file name: &nbsp; "<tt>macro.vm</tt>".
     */
    protected static final String MACRO_VM_FILE_NAME = "macro.vm";

    /**
     * The global Velocity macro file path: &nbsp;
     * "<tt>/click/VM_global_library.vm</tt>".
     */
    protected static final String VM_FILE_PATH = "/click/VM_global_library.vm";

    /** The Velocity writer buffer size. */
    protected static final int WRITER_BUFFER_SIZE = 32 * 1024;

    // -------------------------------------------------------------- Variables

    /** The application mode. */
    protected String applicationMode;

    /** The application character set. */
    protected String charSet;

    /** The application servlet context. */
    protected ServletContext servletContext;

    /** The VelocityEngine instance. */
    protected VelocityEngine velocityEngine = new VelocityEngine();

    /** Cache of velocity writers. */
    protected SimplePool writerPool = new SimplePool(40);

    // --------------------------------------------------------- Public Methods

    /**
     * @see TemplateService#onInit(ServletContext, String, String)
     *
     * @param servletContext the application servlet context
     * @param applicationMode the Click application mode
     * @param charSet the application character set
     * @throws Exception if an error occurs initializing the Template Service
     */
    public void onInit(ServletContext servletContext, String applicationMode,
            String charSet) throws Exception {

        Validate.notNull(servletContext, "Null servletContext parameter");
        Validate.notNull(applicationMode, "Null applicationMode parameter");

        this.servletContext = servletContext;
        this.applicationMode = applicationMode;
        this.charSet = charSet;

        // Set the velocity logging level
        setVelocityLogLevel();

        // Set ServletContext instance for WebappLoader
        String className = ServletContext.class.getName();
        velocityEngine.setApplicationAttribute(className, servletContext);

        // Load velocity properties
        Properties properties = getInitProperties();

        // Initialize VelocityEngine
        velocityEngine.init(properties);

        // Turn down the Velocity logging level
        if (applicationMode.startsWith("pro") || applicationMode.startsWith("dev")) {
            ClickLogger velocityLogger = ClickLogger.getInstance(velocityEngine);
            velocityLogger.setLevel(ClickLogger.WARN_ID);
        }
    }

    /**
     * @see TemplateService#onDestroy()
     */
    public void onDestroy() {
        // Dereference any allocated objects
        velocityEngine = null;
        writerPool = null;
    }

    /**
     * @see TemplateService#renderTemplate(Page, Map, Writer)
     *
     * @param page the page template to render
     * @param model the model to merge with the template and render
     * @param writer the writer to send the merged template and model data to
     * @throws Exception if an error occurs
     */
    public void renderTemplate(Page page, Map model, Writer writer) throws Exception {

        final VelocityContext context = new VelocityContext(model);

        // May throw parsing error if template could not be obtained
        Template template = null;
        if (charSet != null) {
            template = velocityEngine.getTemplate(page.getTemplate(), charSet);

        } else {
            template = velocityEngine.getTemplate(page.getTemplate());
        }

        VelocityWriter velocityWriter = null;

        try {
            velocityWriter = (VelocityWriter) writerPool.get();

            if (velocityWriter == null) {
                velocityWriter =
                    new VelocityWriter(writer, WRITER_BUFFER_SIZE, true);

            } else {
                velocityWriter.recycle(writer);
            }

            template.merge(context, velocityWriter);

        } catch (Exception error) {
            // Exception occured merging template and model. It is possible
            // that some output has already been written, so we will append the
            // error report to the previous output.
            ErrorReport errorReport =
                new ErrorReport(error,
                                page.getClass(),
                                applicationMode.equalsIgnoreCase("production"),
                                page.getContext().getRequest(),
                                servletContext);

            if (velocityWriter == null) {

                velocityWriter =
                    new VelocityWriter(writer, WRITER_BUFFER_SIZE, true);
            }

            velocityWriter.write(errorReport.toString());

            throw error;

        } finally {
            if (velocityWriter != null) {
                // flush and put back into the pool don't close to allow
                // us to play nicely with others.
                velocityWriter.flush();

                // Clear the VelocityWriter's reference to its
                // internal Writer to allow the latter
                // to be GC'd while vw is pooled.
                velocityWriter.recycle(null);

                writerPool.put(velocityWriter);
            }

            writer.flush();
            writer.close();
        }
    }

    /**
     * @see TemplateService#renderTemplate(String, Map, Writer)
     *
     * @param templatePath the path of the template to render
     * @param model the model to merge with the template and render
     * @param writer the writer to send the merged template and model data to
     * @throws Exception if an error occurs
     */
    public void renderTemplate(String templatePath, Map model, Writer writer)
        throws Exception {

        final VelocityContext context = new VelocityContext(model);

        // May throw parsing error if template could not be obtained
        Template template = null;
        if (charSet != null) {
            template = velocityEngine.getTemplate(templatePath, charSet);

        } else {
            template = velocityEngine.getTemplate(templatePath);
        }

        VelocityWriter velocityWriter = null;

        try {
            velocityWriter = (VelocityWriter) writerPool.get();

            if (velocityWriter == null) {
                velocityWriter =
                    new VelocityWriter(writer, WRITER_BUFFER_SIZE, true);

            } else {
                velocityWriter.recycle(writer);
            }

            template.merge(context, velocityWriter);

        } catch (Exception error) {
            // Exception occured merging template and model. It is possible
            // that some output has already been written, so we will append the
            // error report to the previous output.
            ErrorReport errorReport =
                new ErrorReport(error,
                                null,
                                applicationMode.equalsIgnoreCase("production"),
                                null,
                                servletContext);

            if (velocityWriter == null) {

                velocityWriter =
                    new VelocityWriter(writer, WRITER_BUFFER_SIZE, true);
            }

            velocityWriter.write(errorReport.toString());

            throw error;

        } finally {
            if (velocityWriter != null) {
                // flush and put back into the pool don't close to allow
                // us to play nicely with others.
                velocityWriter.flush();

                // Clear the VelocityWriter's reference to its
                // internal Writer to allow the latter
                // to be GC'd while vw is pooled.
                velocityWriter.recycle(null);

                writerPool.put(velocityWriter);
            }

            writer.flush();
            writer.close();
        }
    }

    // ------------------------------------------------------ Protected Methods

    /**
     * Set the Velocity Engine log level.
     */
    protected void setVelocityLogLevel() {

        // Set Velocity log levels
        Integer velocityLogLevel = new Integer(ClickLogger.ERROR_ID);

        if (applicationMode.equalsIgnoreCase("development")) {
            velocityLogLevel = new Integer(ClickLogger.WARN_ID);

        } else if (applicationMode.equalsIgnoreCase("debug")) {
            velocityLogLevel = new Integer(ClickLogger.WARN_ID);

        } else if (applicationMode.equalsIgnoreCase("trace")) {
            velocityLogLevel = new Integer(ClickLogger.INFO_ID);
        }

        velocityEngine.setApplicationAttribute(ClickLogger.LOG_LEVEL,
                                               velocityLogLevel);
    }

    /**
     * Return the Velocity Engine initialization properties.
     *
     * @return the Velocity Engine initialization properties
     * @throws MalformedURLException if a resource cannot be loaded
     */
    protected Properties getInitProperties() throws MalformedURLException {

        final Properties velProps = new Properties();

        // Set default velocity runtime properties.

        velProps.setProperty(RuntimeConstants.RESOURCE_LOADER, "webapp, class");
        velProps.setProperty("webapp.resource.loader.class",
                             WebappLoader.class.getName());
        velProps.setProperty("class.resource.loader.class",
                             ClasspathResourceLoader.class.getName());

        if (applicationMode.startsWith("pro")) {
            velProps.put("webapp.resource.loader.cache", "true");
            velProps.put("webapp.resource.loader.modificationCheckInterval", "0");
            velProps.put("class.resource.loader.cache", "true");
            velProps.put("class.resource.loader.modificationCheckInterval", "0");
            velProps.put("velocimacro.library.autoreload", "false");

        } else {
            velProps.put("webapp.resource.loader.cache", "false");
            velProps.put("class.resource.loader.cache", "false");
            velProps.put("velocimacro.library.autoreload", "true");
        }

        velProps.put(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                     ClickLogger.class.getName());

        // Use 'macro.vm' exists set it as default VM library
        URL macroURL = servletContext.getResource("/" + MACRO_VM_FILE_NAME);
        if (macroURL != null) {
            velProps.put("velocimacro.library", "/" + MACRO_VM_FILE_NAME);

        } else {
            // Else use '/click/VM_global_library.vm' if available.
            URL globalMacroURL = servletContext.getResource(VM_FILE_PATH);
            if (globalMacroURL != null) {
                velProps.put("velocimacro.library", VM_FILE_PATH);

            } else {
                // TODO M.E. 2008-03-29 : Need to document altered behaviour

                // Else use '/WEB-INF/macro.vm' if available.
                String webInfMacroPath = "/WEB-INF/macro.vm";
                URL webInfMacroURL = servletContext.getResource(webInfMacroPath);
                if (webInfMacroURL != null) {
                    velProps.put("velocimacro.library", webInfMacroPath);
                }
            }
        }

        // Set the character encoding
        if (charSet != null) {
            velProps.put("input.encoding", charSet);
        }

        // Load user velocity properties.
        Properties userProperties = new Properties();

        String filename = DEFAULT_TEMPLATE_PROPS;

        InputStream inputStream = servletContext.getResourceAsStream(filename);

        if (inputStream != null) {
            try {
                userProperties.load(inputStream);

            } catch (IOException ioe) {
                String message = "error loading velocity properties file: "
                    + filename;
                ClickLogger.getInstance().error(message, ioe);

            } finally {
                try {
                    inputStream.close();
                } catch (IOException ioe) {
                    // ignore
                }
            }
        }

        // Add user properties.
        Iterator iterator = userProperties.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();

            Object pop = velProps.put(entry.getKey(), entry.getValue());
            if (pop != null && ClickLogger.getInstance().isDebugEnabled()) {
                String message = "user defined property '" + entry.getKey()
                    + "=" + entry.getValue() + "' replaced default propery '"
                    + entry.getKey() + "=" + pop + "'";
                ClickLogger.getInstance().debug(message);
            }
        }

        if (ClickLogger.getInstance().isTraceEnabled()) {
            TreeMap sortedPropMap = new TreeMap();

            Iterator i = velProps.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry entry = (Map.Entry) i.next();
                sortedPropMap.put(entry.getKey(), entry.getValue());
            }

            ClickLogger.getInstance().trace("velocity properties: " + sortedPropMap);
        }

        return velProps;
    }

}
