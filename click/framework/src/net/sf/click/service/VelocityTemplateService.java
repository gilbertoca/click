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
import net.sf.click.util.ClickUtils;
import net.sf.click.util.ErrorReport;

import org.apache.commons.lang.Validate;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.io.VelocityWriter;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.tools.view.servlet.WebappLoader;
import org.apache.velocity.util.SimplePool;

/**
 * Provides a <a target="_blank" href="http://velocity.apache.org//">Velocity</a> TemplateService class.
 *
 * <h3>Configuration</h3>
 * The VelocityTemplateService is the default tempating service used by Click,
 * so it does not require any specific configuration.
 * <p/>
 * However if you wanted to configure this service specifically in your
 * <tt>click.xml</tt> configuration file you would add the following XML element.
 *
 * <pre class="codeConfig">
 * &lt;<span class="red">template-service</span> classname="<span class="blue">net.sf.click.service.VelocityTemplateService</span>"&gt; </pre>
 *
 * @author Malcolm Edgar

 */
public class VelocityTemplateService implements TemplateService {

    // -------------------------------------------------------------- Constants

    /** The logger instance Velocity application attribute key. */
    private static final String LOG_INSTANCE =
        LogChuteAdapter.class.getName() + ".LOG_INSTANCE";

    /** The velocity logger instance Velocity application attribute key. */
    private static final String LOG_LEVEL =
        LogChuteAdapter.class.getName() + ".LOG_LEVEL";

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

    /** The application configuration service. */
    protected ConfigService configService;

    /** The VelocityEngine instance. */
    protected VelocityEngine velocityEngine = new VelocityEngine();

    /** Cache of velocity writers. */
    protected SimplePool writerPool = new SimplePool(40);

    // --------------------------------------------------------- Public Methods

    /**
     * @see TemplateService#onInit(ServletContext, String, String)
     *
     * @param configService the application configuration service instance
     * @throws Exception if an error occurs initializing the Template Service
     */
    public void onInit(ConfigService configService) throws Exception {

        Validate.notNull(configService, "Null configService parameter");

        this.configService = configService;

        // Set the velocity logging level
        Integer logLevel = getInitLogLevel();

        velocityEngine.setApplicationAttribute(LOG_LEVEL, logLevel);

        // Set ConfigService instance for LogChuteAdapter
        velocityEngine.setApplicationAttribute(ConfigService.class.getName(),
                                               configService);

        // Set ServletContext instance for WebappLoader
        velocityEngine.setApplicationAttribute(ServletContext.class.getName(),
                                               configService.getServletContext());

        // Load velocity properties
        Properties properties = getInitProperties();

        // Initialize VelocityEngine
        velocityEngine.init(properties);

        // Turn down the Velocity logging level
        if (configService.isProductionMode() || configService.isProfileMode()) {
            LogChuteAdapter logChuteAdapter = (LogChuteAdapter)
                velocityEngine.getApplicationAttribute(LOG_INSTANCE);
            logChuteAdapter.logLevel = LogChute.WARN_ID;
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
        String charset = configService.getCharset();
        if (charset != null) {
            template = velocityEngine.getTemplate(page.getTemplate(), charset);

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
                                configService.isProductionMode(),
                                page.getContext().getRequest(),
                                configService.getServletContext());

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
        String charset = configService.getCharset();
        if (charset != null) {
            template = velocityEngine.getTemplate(templatePath, charset);

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
                                configService.isProductionMode(),
                                null,
                                configService.getServletContext());

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
     * Return the Velocity Engine initialization log level.
     *
     * @return the Velocity Engine initialization log level
     */
    protected Integer getInitLogLevel() {

        // Set Velocity log levels
        Integer initLogLevel = new Integer(LogChute.ERROR_ID);
        String mode = configService.getApplicationMode();

        if (mode.equals(ConfigService.MODE_DEVELOPMENT)) {
            initLogLevel = new Integer(LogChute.WARN_ID);

        } else if (mode.equals(ConfigService.MODE_DEBUG)) {
            initLogLevel = new Integer(LogChute.WARN_ID);

        } else if (mode.equals(ConfigService.MODE_TRACE)) {
            initLogLevel = new Integer(LogChute.INFO_ID);
        }

        return initLogLevel;
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

        if (configService.isProductionMode() || configService.isProfileMode()) {
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
                     LogChuteAdapter.class.getName());

        // Use 'macro.vm' exists set it as default VM library
        ServletContext servletContext = configService.getServletContext();
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
        String charset = configService.getCharset();
        if (charset != null) {
            velProps.put("input.encoding", charset);
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
                configService.getLogService().error(message, ioe);

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

            LogService logService = configService.getLogService();
            if (pop != null && logService.isDebugEnabled()) {
                String message = "user defined property '" + entry.getKey()
                    + "=" + entry.getValue() + "' replaced default propery '"
                    + entry.getKey() + "=" + pop + "'";
                logService.debug(message);
            }
        }

        ConfigService configService = ClickUtils.getConfigService(servletContext);
        LogService logger = configService.getLogService();
        if (logger.isTraceEnabled()) {
            TreeMap sortedPropMap = new TreeMap();

            Iterator i = velProps.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry entry = (Map.Entry) i.next();
                sortedPropMap.put(entry.getKey(), entry.getValue());
            }

            logger.trace("velocity properties: " + sortedPropMap);
        }

        return velProps;
    }

    // ---------------------------------------------------------- Inner Classes

    /**
     * Provides a Velocity <tt>LogChute</tt> adapter class around the application
     * log service to enable to the Velocity Runtime to log to the application
     * LogService.
     *
     *  @author Malcolm Edgar
     */
    public static class LogChuteAdapter implements LogChute {

        private static final String MSG_PREFIX = "Velocity: ";

        /** The application configuration service. */
        protected ConfigService configService;

        /** The application log service. */
        protected LogService logger;

        /** The log level. */
        protected int logLevel;

        /**
         * Initialize the logger instance for the Velocity runtime. This method
         * is invoked by the Velocity runtime.
         *
         * @see LogChute#init(RuntimeServices)
         *
         * @param rs the Velocity runtime services
         * @throws Exception if an initialization error occurs
         */
        public void init(RuntimeServices rs) throws Exception {

            // Swap the default logger instance with the global application logger
            ConfigService configService = (ConfigService)
                rs.getApplicationAttribute(ConfigService.class.getName());

            this.logger = configService.getLogService();

            Integer level = (Integer) rs.getApplicationAttribute(LOG_LEVEL);
            if (level instanceof Integer) {
                logLevel = level.intValue();

            } else {
                String msg = "Could not retrieve LOG_LEVEL from Runtime attributes";
                throw new IllegalStateException(msg);
            }

            rs.setApplicationAttribute(LOG_INSTANCE, this);
        }

        /**
         * Tell whether or not a log level is enabled.
         *
         * @see LogChute#isLevelEnabled(int)
         *
         * @param level the logging level to test
         * @return true if the given logging level is enabled
         */
        public boolean isLevelEnabled(int level) {
            if (level <= LogChute.TRACE_ID && logger.isTraceEnabled()) {
                return true;

            } else if (level <= LogChute.DEBUG_ID && logger.isDebugEnabled()) {
                return true;

            } else if (level <= LogChute.INFO_ID && logger.isInfoEnabled()) {
                return true;

            } else if (level == LogChute.WARN_ID || level == LogChute.ERROR_ID) {
                return true;

            } else {
                return false;
            }
        }

        /**
         * Log the given message and optional error at the specified logging level.
         *
         * @see LogChute#log(int, java.lang.String)
         *
         * @param level the logging level
         * @param message the message to log
         */
        public void log(int level, String message) {
            if (level < logLevel) {
                return;
            }

            if (level == TRACE_ID) {
                logger.trace(MSG_PREFIX + message);

            } else if (level == DEBUG_ID) {
                logger.debug(MSG_PREFIX + message);

            } else if (level == INFO_ID) {
                logger.info(MSG_PREFIX + message);

            } else if (level == WARN_ID) {
                logger.warn(MSG_PREFIX + message);

            } else if (level == ERROR_ID) {
                logger.error(MSG_PREFIX + message);

            } else {
                // TODO:
            }
        }

        /**
         * Log the given message and optional error at the specified logging level.
         * <p/>
         * If you need to customise the Click and Velocity runtime logging for your
         * application modify this method.
         *
         * @see LogChute#log(int, java.lang.String, java.lang.Throwable)
         *
         * @param level the logging level
         * @param message the message to log
         * @param error the optional error to log
         */
        public void log(int level, String message, Throwable error) {
            if (level < logLevel) {
                return;
            }

            if (level == TRACE_ID) {
                logger.trace(MSG_PREFIX + message, error);

            } else if (level == DEBUG_ID) {
                logger.debug(MSG_PREFIX + message, error);

            } else if (level == INFO_ID) {
                logger.info(MSG_PREFIX + message, error);

            } else if (level == WARN_ID) {
                logger.warn(MSG_PREFIX + message, error);

            } else if (level == ERROR_ID) {
                logger.error(MSG_PREFIX + message, error);

            } else {
                // TODO:
            }
        }

    }

}
