/**
 * Copyright 2004-2005 Malcolm A. Edgar
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
package net.sf.click.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.sf.click.Page;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.exception.ParseErrorException;

/**
 * Provides miscellaneous String and Stream utility methods.
 *
 * @author Malcolm Edgar
 */
public class ClickUtils {

    /** The number of template lines to display in error report. */
    protected static final int NUMBER_TEMPLATE_LINES = 8;

    // --------------------------------------------------------- Public Methods

    /**
     * Close the given output stream and ignore any exceptions thrown.
     *
     * @param stream the output stream to close.
     */
    public static void close(OutputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ex) {
                // Ignore.
            }
        }
    }

    /**
     * Close the given input stream and ignore any exceptions thrown.
     *
     * @param stream the input stream to close.
     */
    public static void close(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ex) {
                // Ignore.
            }
        }
    }

    /**
     * Return an encoded version of the <tt>Serializble</tt> object. The object
     * will be serialized, compressed and Base 64 encoded.
     *
     * @param object the object to encode
     * @return a serialized, compressed and Base 64 string encoding of the
     * given object
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if the object parameter is null, or if
     *      the object is not Serializable
     */
    public static String encode(Object object) throws IOException {
        if (object == null) {
            throw new IllegalArgumentException("null object parameter");
        }
        if (!(object instanceof Serializable)) {
            throw new IllegalArgumentException("parameter not Serializable");
        }

        ByteArrayOutputStream bos = null;
        GZIPOutputStream gos = null;
        ObjectOutputStream oos = null;

        try {
            bos = new ByteArrayOutputStream();
            gos = new GZIPOutputStream(bos);
            oos = new ObjectOutputStream(gos);

            oos.writeObject(object);
            oos.close();
        } finally {
            close(oos);
            close(gos);
            close(bos);
        }

        Base64 base64 = new Base64();

        try {
            byte[] byteData = base64.encode(bos.toByteArray());

            return new String(byteData);

        } catch (EncoderException ee) {
            String message = "error occured Base64 encoding: " + object;
            throw new IOException(message);
        }
    }

    /**
     * Return an object from the {@link #encode(Object)} string.
     *
     * @param string the encoded string
     * @return an object from the encoded
     * @throws ClassNotFoundException if the class could not be instantiated
     * @throws IOException if an data I/O error occurs
     */
    public static Object decode(String string)
            throws ClassNotFoundException, IOException {

        Base64 base64 = new Base64();
        byte[] byteData = null;

        try {
            byteData = base64.decode(string.getBytes());

        } catch (DecoderException de) {
            String message = "error occured Base64 decoding: " + string;
            throw new IOException(message);
        }

        ByteArrayInputStream bis = null;
        GZIPInputStream gis = null;
        ObjectInputStream ois = null;
        try {
            bis = new ByteArrayInputStream(byteData);
            gis = new GZIPInputStream(bis);
            ois = new ObjectInputStream(gis);

            return ois.readObject();

        } finally {
            close(ois);
            close(gis);
            close(bis);
        }
    }

    /**
     * Return a error report HTML &lt;div&gt; element for the given error and
     * page. The HTML &lt;div&gt; element 'id' and 'class' attribute values are
     * 'errorReport'.
     *
     * @param error the page error
     * @param page the page which caused the error
     * @return a error HTML display
     */
    public static String getErrorReport(Throwable error, Page page) {
        StringBuffer buffer = new StringBuffer(10 * 1024);

        Throwable cause = null;
        if (error instanceof ServletException) {
            ServletException se = (ServletException) error;
            if (se.getRootCause() != null) {
                cause = se.getRootCause();
            } else if (se.getCause() != null) {
                cause = se.getCause();
            } else {
                cause = error;
            }
        } else {
            if (error.getCause() != null) {
                cause = error.getCause();
            } else {
                cause = error;
            }
        }

        HttpServletRequest request = page.getContext().getRequest();
        
        ErrorInfo errorInfo = 
            new ErrorInfo(cause, page.getContext().getServletContext());

        String errorSource = getErrorSource(errorInfo);

        buffer.append("<div id='errorReport' class='errorReport'>\n");

        buffer.append("<table border='1' cellspacing='1' cellpadding='4' width='100%'>");
        if (errorInfo.isParseError) {

            buffer.append("<tr><td valign='top' width='12%'><b>Message</b></td><td>");
            buffer.append(errorInfo.message);
            buffer.append("</td></tr>");
            
            buffer.append("<tr><td valign='top' colspan='2'><b>Template:</b>&nbsp;");
            buffer.append(errorInfo.sourceName);
            buffer.append("<br><span style='font-family: Courier New, courier;'><br/>");
            buffer.append(errorSource);
            buffer.append("</span></td></tr>");
            buffer.append("</table>");
            buffer.append("<br/>");

        } else {
            buffer.append("<tr><td colspan='2' style='color:white; background-color: navy; font-weight: bold'>Exception</td></tr>");
            buffer.append("<tr><td width='12%'><b>Message</b></td><td>");
            buffer.append(errorInfo.message);
            buffer.append("</td></tr>");
            buffer.append("<tr><td width='12%'><b>Class</b></td><td>");
            buffer.append(cause.getClass().getName());
            buffer.append("</td></tr>");
            buffer.append("<tr><td valign='top' colspan='2'><b>Stack trace</b><br>");
            buffer.append("<span style='font-family: Courier New, courier;'>");
            buffer.append(errorInfo.getStackTrace());
            buffer.append("</span></td></tr>");
            buffer.append("</table>");
            buffer.append("<br/>");
        }

        buffer.append("<table border='1' cellspacing='1' cellpadding='4' width='100%'>");
        buffer.append("<tr><td colspan='2' style='color:white; background-color: navy; font-weight: bold'>Page</td></tr>");
        buffer.append("<tr><td width='12%'><b>Classname</b></td><td>");
        buffer.append(page.getClass().getName());
        buffer.append("</td></tr>");
        buffer.append("<tr><td width='12%'><b>Path</b></td><td>");
        buffer.append(page.getPath());
        buffer.append("</td></tr>");
        buffer.append("<tr><td><b width='12%'>Template</b></td><td>");
        buffer.append(page.getTemplate());
        buffer.append("</td></tr>");
        buffer.append("</table>");
        buffer.append("<br/>");

        buffer.append("<table border='1' cellspacing='1' cellpadding='4' width='100%'>");
        buffer.append("<tr><td colspan='2' style='color:white; background-color: navy; font-weight: bold'>Request</td></tr>");

        TreeMap requestAttributes = new TreeMap();
        Enumeration attributeNames = request.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement().toString();
            requestAttributes.put(name, request.getAttribute(name));
        }
        buffer.append("<tr><td width='12%' valign='top'><b>Attributes</b></td><td>");
        writeMap(requestAttributes, buffer);
        buffer.append("</td></tr>");

        buffer.append("<tr><td width='12%'><b>Auth Type</b></td><td>");
        buffer.append(request.getAuthType());
        buffer.append("</td></tr>");

        buffer.append("<tr><td width='12%'><b>Context Path</b></td><td>");
        buffer.append("<a href='");
        buffer.append(request.getContextPath());
        buffer.append("'>");
        buffer.append(request.getContextPath());
        buffer.append("</a>");
        buffer.append("</td></tr>");

        TreeMap requestHeaders = new TreeMap();
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement().toString();
            requestHeaders.put(name, request.getHeader(name));
        }
        buffer.append("<tr><td width='12%' valign='top'><b>Headers</b></td><td>");
        writeMap(requestHeaders, buffer);
        buffer.append("</td></tr>");

        buffer.append("<tr><td width='12%'><b>Query</b></td><td>");
        buffer.append(request.getQueryString());
        buffer.append("</td></tr>");

        buffer.append("<tr><td width='12%'><b>Method</b></td><td>");
        buffer.append(request.getMethod());
        buffer.append("</td></tr>");

        TreeMap requestParams = new TreeMap();
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement().toString();
            requestParams.put(name, request.getParameter(name));
        }
        buffer.append("<tr><td width='12%' valign='top'><b>Parameters</b></td><td>");
        writeMap(requestParams, buffer);
        buffer.append("</td></tr>");

        buffer.append("<tr><td width='12%'><b>Remote User</b></td><td>");
        buffer.append(request.getRemoteUser());
        buffer.append("</td></tr>");

        buffer.append("<tr><td width='12%' valign='top'><b>URI</b></td><td>");
        buffer.append("<a href='");
        buffer.append(request.getRequestURI());
        buffer.append("'>");
        buffer.append(request.getRequestURI());
        buffer.append("</a>");
        buffer.append("</td></tr>");

        buffer.append("<tr><td><b width='12%'>URL</b></td><td>");
        buffer.append("<a href='");
        buffer.append(request.getRequestURL());
        buffer.append("'>");
        buffer.append(request.getRequestURL());
        buffer.append("</a>");
        buffer.append("</td></tr>");

        TreeMap sessionAttributes = new TreeMap();
        if (page.getContext().hasSession()) {
            HttpSession session = request.getSession();
            attributeNames = session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String name = attributeNames.nextElement().toString();
                sessionAttributes.put(name, session.getAttribute(name));
            }
        }
        buffer.append("<tr><td width='12%' valign='top'><b>Session</b></td><td>");
        writeMap(sessionAttributes, buffer);
        buffer.append("</td></tr>");
        buffer.append("</table>\n");

        buffer.append("</div>\n");

        return buffer.toString();
    }

    /**
     * Invoke the named method on the given object and return the boolean result.
     *
     * @see net.sf.click.Control#setListener(Object, String)
     *
     * @param listener the object with the method to invoke
     * @param method the name of the method to invoke
     * @return true if the listener method returned true
     */
    public static boolean invokeListener(Object listener, String method) {
        if (listener == null) {
            throw new IllegalArgumentException("Null listener parameter");
        }
        if (method == null) {
            throw new IllegalArgumentException("Null method parameter");
        }

        Method targetMethod = null;
        try {
            targetMethod = listener.getClass().getMethod(method, null);

            Object result = targetMethod.invoke(listener, null);

            if (result instanceof Boolean) {
                return ((Boolean)result).booleanValue();

            } else {
                String msg =
                    "Invalid listener method, missing boolean return type: " +
                    targetMethod;
                throw new RuntimeException(msg);
            }

        } catch (InvocationTargetException ite) {

            Throwable e = ite.getTargetException();
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;

            } else if (e instanceof Exception) {
                String msg =
                    "Exception occured invoking public method: " + targetMethod;

                throw new RuntimeException(msg, e);

            } else if (e instanceof Error) {
                String msg =
                    "Error occured invoking public method: " + targetMethod;

                throw new RuntimeException(msg, e);

            } else {
                String msg =
                    "Error occured invoking public method: " + targetMethod;

                throw new RuntimeException(msg, e);
            }

        } catch (Exception e) {
            String msg =
                "Exception occured invoking public method: " + targetMethod;

            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Render the given Map of HTML attributes to the StringBuffer.
     *
     * @param attributes the Map of HTML attributes
     * @param buffer the StringBuffer to render to
     */
    public static void renderAttributes(Map attributes, StringBuffer buffer) {
        if (attributes != null) {
            for (Iterator i = attributes.keySet().iterator(); i.hasNext();) {
                String name = i.next().toString();
                String value = attributes.get(name).toString();
                buffer.append(" ");
                buffer.append(name);
                buffer.append("='");
                buffer.append(value);
                buffer.append("'");
            }
        }
    }

    /**
     * Return the getter method name for the given property name.
     *
     * @param property the property name
     * @return the setter method name for the given property name.
     */
    public static String toGetterName(String property) {
        StringBuffer buffer = new StringBuffer(property.length() + 3);

        buffer.append("get");
        buffer.append(Character.toUpperCase(property.charAt(0)));
        buffer.append(property.substring(1));

        return buffer.toString();
    }

    /**
     * Return a field name string from the given field label.
     * <p/>
     * A label of " OK do it!" is returned as "okDoIt".
     *
     * @param label the field label or caption
     * @return a field name string from the given field label
     */
    public static String toName(String label) {
        if (label == null) {
            throw new IllegalArgumentException("Null label parameter");
        }

        boolean doneFirstLetter = false;
        boolean lastCharBlank = false;

        StringBuffer buffer = new StringBuffer(label.length());
        for (int i = 0, size = label.length(); i < size; i++) {
            char aChar = label.charAt(i);

            if (aChar != ' ') {
                if (Character.isJavaIdentifierPart(aChar)) {
                    if (lastCharBlank) {
                        if (doneFirstLetter) {
                            buffer.append(Character.toUpperCase(aChar));
                            lastCharBlank = false;
                        } else {
                            buffer.append(Character.toLowerCase(aChar));
                            lastCharBlank = false;
                            doneFirstLetter = true;
                        }
                    } else {
                        if (doneFirstLetter) {
                            buffer.append(Character.toLowerCase(aChar));
                        } else {
                            buffer.append(Character.toLowerCase(aChar));
                            doneFirstLetter = true;
                        }
                    }
                }
            } else {
                lastCharBlank = true;
            }
        }

        return buffer.toString();
    }

    /**
     * Return a HTML encode stack trace string from the given error.
     *
     * @param error the error to get the stack trace from
     * @return a HTML encode stack trace string.
     */
    public static String toStackTrace(Throwable error) {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        error.printStackTrace(pw);

        StringTokenizer tokenizer = new StringTokenizer(sw.toString(), "\n");

        StringBuffer buffer = new StringBuffer(400);

        if (tokenizer.hasMoreTokens()) {
            buffer.append(StringEscapeUtils.escapeHtml(tokenizer.nextToken()));
            buffer.append("<br/>");
        }

        while (tokenizer.hasMoreTokens()) {
            buffer.append("&nbsp;&nbsp;&nbsp;");
            buffer.append(StringEscapeUtils.escapeHtml(tokenizer.nextToken()));
            buffer.append("<br/>");
        }

        return buffer.toString();
    }

    /**
     * Return the setter method name for the given property name.
     *
     * @param property the property name
     * @return the setter method name for the given property name.
     */
    public static String toSetterName(String property) {
        StringBuffer buffer = new StringBuffer(property.length() + 3);

        buffer.append("set");
        buffer.append(Character.toUpperCase(property.charAt(0)));
        buffer.append(property.substring(1));

        return buffer.toString();
    }

    // ------------------------------------------------------ Protected Methods

    /**
     * Return the HTML formatted parse error error message from the given error.
     *
     * @param error the Velocity ParseErrorException error
     * @return the HTML formatted error message
     */
    protected static String getParseMessage(Throwable error) {

        StringTokenizer tokenizer =
            new StringTokenizer(error.getMessage(), "\n\r");

        StringBuffer buffer = new StringBuffer();
        buffer.append(tokenizer.nextToken());
        buffer.append(" ");

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            token = token.replace('"', ' ');
            if (token.endsWith("...")) {
                buffer.append(token.substring(0, token.length() - 3));
            } else {
                buffer.append(token);
            }
        }

        return StringEscapeUtils.escapeHtml(buffer.toString());
    }
    
    /**
     * Return a HTML rendered section of the source error with the error
     * line highlighted.
     *
     * @param errorInfo the parse error
     * @return a HTML rendered section of the parsing error page template
     */
    protected static String getErrorSource(ErrorInfo errorInfo) {

        StringBuffer buffer = new StringBuffer(5*1024);

        if (errorInfo.sourceReader == null) {
            buffer.append("Page template not available.");
            return buffer.toString();
        }
        
        final String errorLineStyle = "style='background-color:yellow;'";
        final String errorCharSpan = 
            "<span style='color:red;text-decoration:underline;font-weight:bold;'>";

        try {
            String line = errorInfo.sourceReader.readLine();
            
            while (line != null) {
                boolean isErrorLine = 
                    errorInfo.sourceReader.getLineNumber() == errorInfo.lineNumber;
                
                // Start div tag
                buffer.append("<div ");
                if (isErrorLine) {
                    buffer.append(errorLineStyle);
                }
                buffer.append(">");
                
                // Write out line number
                String lineStr = "" + errorInfo.sourceReader.getLineNumber();
                int numberSpace = 3 - lineStr.length();
                for (int i = 0; i < numberSpace; i++) {
                    buffer.append("&nbsp;");
                }
                if (isErrorLine) {
                    buffer.append("<b>");
                }
                buffer.append(errorInfo.sourceReader.getLineNumber());
                if (isErrorLine) {
                    buffer.append("</b>");
                }
                buffer.append(":&nbsp;&nbsp;");
                
                // Write out line content
                if (isErrorLine) {
                    StringBuffer htmlLine = new StringBuffer(line.length() * 2);
                    for (int i = 0; i < line.length(); i++) {
                        if (i == errorInfo.columnNumber - 1) {
                            htmlLine.append(errorCharSpan);
                            htmlLine.append(line.charAt(i));
                            htmlLine.append("</span>");
                        } else {
                            htmlLine.append(line.charAt(i));
                        }
                    }
                    buffer.append(htmlLine.toString());
                    
                } else {
                    buffer.append(StringEscapeUtils.escapeHtml(line));
                }
                
                // Close div tag
                buffer.append("</div>\n");
                
                line = errorInfo.sourceReader.readLine();
            }
            
        } catch (IOException ioe) {
            buffer.append("Could not load page template: " + ioe);
        } finally {
            try {
                errorInfo.sourceReader.close();
            } catch (IOException ioe) {
                // do nothing
            }
        }

        return buffer.toString();
    }

    /**
     * Write out the map name value pairs as name=value lines to the string
     * buffer.
     *
     * @param map the Map of name value pairs
     * @param buffer the string buffer to write out the values to
     */
    protected static void writeMap(Map map, StringBuffer buffer) {
        for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
            String name = i.next().toString();
            String value = map.get(name).toString();
            buffer.append(name);
            buffer.append("=");
            buffer.append(StringEscapeUtils.escapeHtml(value));
            buffer.append("</br>");
        }
        if (map.isEmpty()) {
            buffer.append("&nbsp;");
        }
    }
    
    /**
     * Provides Error details for the display of error messages.
     *
     * @author Malcolm Edgar
     */
    protected static class ErrorInfo {
        
        /** The cause of error. */
        protected final Throwable cause;
        
        /** The error is Velocity parsing exception. */
        protected final boolean isParseError;
        
        /** The name of the error source. */
        protected final String sourceName;
        
        /** The error souce LineNumberReader */
        protected LineNumberReader sourceReader;
        
        /** The line number of the error, or -1 if not defined. */
        protected final int lineNumber;
        
        /** The column number of the error, or -1 if not defined. */
        protected final int columnNumber;
        
        /** The error message. */
        protected final String message;
        
        /**
         * Create a ErrorInfo instance from the given error and ServletContext.
         * 
         * @param error the source of the error
         * @param context the ServletContext
         */
        protected ErrorInfo(Throwable error, ServletContext context) {
            cause = error;
            
            isParseError = error instanceof ParseErrorException;
            
            if (isParseError) {
                ParseErrorException pee = (ParseErrorException) error;
                sourceName = pee.getTemplateName();
                lineNumber = pee.getLineNumber();
                columnNumber = pee.getColumnNumber();
                
                message = getParseMessage(error);
                
                InputStream is = 
                    context.getResourceAsStream(pee.getTemplateName());
                
                LineNumberReader lineReader = 
                    new LineNumberReader(new InputStreamReader(is));
                
            } else {
                sourceName = null;
                lineNumber = -1;
                columnNumber = -1;
                
                String value = 
                    (error.getMessage() != null) ? error.getMessage() : "null";
                message = StringEscapeUtils.escapeHtml(value);
            }
        }
        
        protected String getStackTrace() {
            return toStackTrace(cause);
        }
    }
}
