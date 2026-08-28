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
import java.util.List;
import java.util.Locale;
import org.apache.click.control.TextField;
import org.apache.click.element.CssImport;
import org.apache.click.element.Element;
import org.apache.click.element.JsImport;
import org.apache.click.element.JsScript;
import org.apache.click.util.ClickUtils;
import org.apache.click.util.HtmlStringBuffer;

/**
 *
 * Provides a modern HTML5 LocalDate Field control: <input type='date'>.
 * Natively handles binding with java.time.LocalDate properties.
 */
public class JQueryLocalDateField extends TextField {

    private static final long serialVersionUID = 1L;
    private static final String JQUERY_UI_VERSION = "1.14.2";
    private static final String JQUERY_VERSION = "3.7.1";
    /**
     * The date format pattern value.
     */
    protected String formatPattern;

    protected LocalDate localDate;

    // ----------------------------------------------------------- Constructors
    public JQueryLocalDateField(String name) {
        super(name);
    }

    public JQueryLocalDateField(String name, String label) {
        super(name, label);
    }

    public JQueryLocalDateField(String name, boolean required) {
        this(name);
        setRequired(required);
    }

    public JQueryLocalDateField(String name, String label, boolean required) {
        super(name, label, required);
    }

    public JQueryLocalDateField() {
        super();
    }

    // --------------------------------------------------------- Private/Protected Methods
    protected Locale getLocale() {
        return getContext().getLocale();
    }

    // --------------------------------------------------------- Public Methods
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
            super.setValue(localDate.format(DateTimeFormatter.ofPattern(getFormatPattern(), getLocale())));
        } else {
            super.setValue(null);
        }
    }

    /**
     * Return the LocalDate format pattern. If the LocalDate format pattern is
     * not defined it will be loaded through the method, {@link #getMessage()}.
     *
     * @return the date format pattern
     */
    public String getFormatPattern() {
        if (formatPattern == null) {
            formatPattern = getMessage("jquery-localdate-format-pattern");
            if (formatPattern == null) {
                formatPattern = "dd/MM/yyyy";
            }
        }
        return formatPattern;
    }

    /**
     * Set the LocalDate format pattern.
     *
     * @param pattern the DateTimeFormatter pattern
     */
    public void setFormatPattern(String pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("Null pattern parameter");
        }
        formatPattern = pattern;
    }

    @Override
    public void setValue(String value) {
        if (value != null && value.length() > 0) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(getFormatPattern(), getLocale());
                this.localDate = LocalDate.parse(value.trim(), formatter);
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

    @Override
    public void validate() {
        String formatPattern = getFormatPattern();

        if (formatPattern == null) {
            String msg = "dateFormat attribute is null for field: " + getName();
            throw new IllegalStateException(msg);
        }

        super.validate();

        if (isValid() && getValue().length() > 0) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(getFormatPattern(), getLocale());
                LocalDate.parse(getValue().trim(), formatter);
            } catch (DateTimeParseException pe) {
                // Uses the resource bundle error key
                Object[] args = new Object[]{getErrorLabel(), "yyyy-MM-dd"};
                setError(getMessage("jquery-localdate-format-error", args));
            }
        }
    }

    @Override
    public List<Element> getHeadElements() {
        if (headElements == null) {
            headElements = super.getHeadElements();
            String versionIndicator = ClickUtils.getResourceVersionIndicator(getContext());
            // Core jQuery UI Imports
            headElements.add(new JsImport("/click/jquery/" + JQUERY_VERSION + "/jquery.min.js", versionIndicator));
            headElements.add(new JsImport("/click/jquery/ui/" + JQUERY_UI_VERSION + "/jquery-ui.min.js", versionIndicator));
            headElements.add(new CssImport("/click/jquery/ui/" + JQUERY_UI_VERSION + "/jquery-ui.min.css", versionIndicator));
        }
        String fieldId = getId();
        JsScript setupScript = new JsScript();
        setupScript.setId(fieldId + "-datepicker-setup");
        setupScript.setExecuteOnDomReady(true);

        String jqPattern = getFormatPattern().replace("yyyy", "yy").replace("MM", "mm");

        HtmlStringBuffer scriptBuffer = new HtmlStringBuffer(150);
        scriptBuffer.append("$('#").append(fieldId).append("').datepicker({");
        scriptBuffer.append(" dateFormat: '").append(jqPattern).append("'");
        scriptBuffer.append(" });");

        setupScript.setContent(scriptBuffer.toString());
        headElements.add(setupScript);
        return headElements;
    }

    @Override
    public void render(HtmlStringBuffer buffer) {
        // Set default title
        if (getTitle() == null) {
            setTitle(getMessage("jquery-localdate-title", getFormatPattern()));
        }
        
        // CLK-58 Change from "date" to "text" to hand over control to your JS engine
        buffer.append("<input type=\"text\"");
        buffer.appendAttribute("name", getName());
        buffer.appendAttribute("id", getId());

        // Render the localized text representation of the field state safely
        if (getValue() != null && getValue().length() > 0) {
            buffer.appendAttribute("value", getValue());
        }

        appendAttributes(buffer);
        buffer.append("/>");
    }
}
