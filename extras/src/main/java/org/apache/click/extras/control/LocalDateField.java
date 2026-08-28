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
package org.apache.click.extras.control;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.apache.click.control.TextField;
import org.apache.click.util.HtmlStringBuffer;

/**
 *
 * Provides a modern HTML5 LocalDate Field control: <input type='date'>.
 * Natively handles binding with java.time.LocalDate properties.
 */
public class LocalDateField extends TextField {

    private static final long serialVersionUID = 1L;

    // HTML5 date inputs strictly require and return the ISO-8601 format (yyyy-MM-dd)
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    protected LocalDate localDate;

    // ----------------------------------------------------------- Constructors
    public LocalDateField(String name) {
        super(name);
    }

    public LocalDateField(String name, String label) {
        super(name, label);
    }

    public LocalDateField(String name, boolean required) {
        this(name);
        setRequired(required);
    }

    public LocalDateField(String name, String label, boolean required) {
        super(name, label, required);
    }

    public LocalDateField() {
        super();
    }

    // ------------------------------------------------------- Public Attributes
    /**
     * Return the field LocalDate value, or null if value was empty or a parsing
     * error occurred.
     *
     * @return the field LocalDate value
     */
    public LocalDate getLocalDate() {
        return localDate;
    }

    /**
     * Set the field LocalDate value.
     *
     * @param localDate the LocalDate value to set
     */
    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
        if (localDate != null) {
            super.setValue(localDate.format(ISO_FORMATTER));
        } else {
            super.setValue(null);
        }
    }

    @Override
    public void setValue(String value) {
        if (value != null && value.length() > 0) {
            try {
                this.localDate = LocalDate.parse(value.trim(), ISO_FORMATTER);
            } catch (DateTimeParseException e) {
                this.localDate = null;
            }
        } else {
            this.localDate = null;
        }
        super.setValue(value);
    }

    @Override
    public Object getValueObject() {
        return getLocalDate();
    }

    @Override
    public void setValueObject(Object object) {
        if (object == null) {
            setLocalDate(null);
        } else if (object instanceof LocalDate) {
            setLocalDate((LocalDate) object);
        } else {
            String msg = "Invalid object class: " + object.getClass().getName();
            throw new IllegalArgumentException(msg);
        }
    }

    // --------------------------------------------------------- Public Methods
    @Override
    public void validate() {
        super.validate();

        if (isValid() && getValue().length() > 0) {
            try {
                LocalDate.parse(getValue().trim(), ISO_FORMATTER);
            } catch (DateTimeParseException pe) {
                Object[] args = new Object[]{getErrorLabel(), "yyyy-MM-dd"};
                setError(getMessage("localdate-format-error", args));
            }
        }
    }

    @Override
    public void render(HtmlStringBuffer buffer) {
        // Set default title
        if (getTitle() == null) {
            setTitle(getMessage("localdate-title", ISO_FORMATTER));
        }
        
        buffer.append("<input type=\"date\"");
        buffer.appendAttribute("name", getName());
        buffer.appendAttribute("id", getId());

        if (getLocalDate() != null) {
            buffer.appendAttribute("value", getLocalDate().format(ISO_FORMATTER));
        } else if (getValue().length() > 0) {
            buffer.appendAttribute("value", getValue());
        }

        appendAttributes(buffer);
        buffer.append("/>");
    }
}
