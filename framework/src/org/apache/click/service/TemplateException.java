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

/**
 * Provides a template exception class for use by the template service. This is
 * used to wrap TemplateService exceptions.
 */
public class TemplateException extends Exception {

    private static final long serialVersionUID = 1L;

    /** The template column number where this exception occurred. */
    private int columnNumber;

    /** The template the line number where this exception occurred. */
    private int lineNumber;

    /** The Template name where this exception occurred. */
    private String templateName;

    // Constructors -----------------------------------------------------------

    /**
     * Create a template service exception with the given cause.
     *
     * @param cause the underlying cause of the template service error
     */
    public TemplateException(Exception cause) {
        super(cause);
    }

    /**
     * Create a template service exception with the given cause, template name,
     * line number and column number.
     *
     * @param cause the underlying cause of the template service error
     * @param templateName the name of the template which cause the error
     * @param lineNumber the template error line number
     * @param columnNumber the template error column number
     */
    public TemplateException(Exception cause, String templateName,
            int lineNumber, int columnNumber) {

        super(cause);

        this.templateName = templateName;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    // Public Methods ------------------------------------------------------

    /**
     * Return the template column number where this exception occurred.
     *
     * @return the template column number where this exception occurred
     */
    public int getColumnNumber() {
        return columnNumber;
    }

    /**
     * Return the template the line number where this exception occurred.
     *
     * @return the template the line number where this exception occurred
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Return the Template name where this exception occurred.
     *
     * @return the Template name where this exception occurred
     */
    public String getTemplateName() {
        return templateName;
    }

    /**
     * @see Object#toString()
     *
     * @return the string representation of this error
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getName());
        builder.append(": ");
        if (getLocalizedMessage() != null) {
            builder.append(getLocalizedMessage());
        }

        if (getTemplateName() != null) {
            builder.append(" [templateName=");
            builder.append(getTemplateName());
            builder.append(",lineNumber=");
            builder.append(getLineNumber());
            builder.append(",columnNumber=");
            builder.append(getColumnNumber());
            builder.append("]");
        }

        return builder.toString();
    }

}
