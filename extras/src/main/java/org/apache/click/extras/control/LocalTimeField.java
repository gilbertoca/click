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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.apache.click.control.TextField;
import org.apache.click.util.HtmlStringBuffer;

/**
 * Provides a modern HTML5 LocalTime Field control: <input type='time'>.
 */
public class LocalTimeField extends TextField {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

    protected LocalTime localTime;

    public LocalTimeField(String name) {
        super(name);
    }

    public LocalTimeField(String name, String label) {
        super(name, label);
    }

    public LocalTimeField() {
        super();
    }

    public LocalTime getLocalTime() {
        return localTime;
    }

    public void setLocalTime(LocalTime localTime) {
        this.localTime = localTime;
        if (localTime != null) {
            super.setValue(localTime.format(ISO_FORMATTER));
        } else {
            super.setValue(null);
        }
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        if (value != null && value.trim().length() > 0) {
            try {
                this.localTime = LocalTime.parse(value.trim(), ISO_FORMATTER);
            } catch (DateTimeParseException e) {
                this.localTime = null;
            }
        } else {
            this.localTime = null;
        }
    }

    @Override
    public Object getValueObject() {
        return getLocalTime();
    }

    @Override
    public void setValueObject(Object object) {
        if (object == null) {
            setLocalTime(null);
        } else if (object instanceof LocalTime) {
            setLocalTime((LocalTime) object);
        } else {
            String msg = "Invalid object class: " + object.getClass().getName();
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public void validate() {
        super.validate();

        if (isValid() && getValue() != null && getValue().trim().length() > 0) {
            try {
                LocalTime.parse(getValue().trim(), ISO_FORMATTER);
            } catch (DateTimeParseException pe) {
                Object[] args = new Object[]{getErrorLabel(), "HH:mm"};
                setError(getMessage("localtime-format-error", args));
            }
        }
    }

    @Override
    public void render(HtmlStringBuffer buffer) {
        // Set default title
        if (getTitle() == null) {
            setTitle(getMessage("localdate-title", ISO_FORMATTER));
        }
                
        buffer.append("<input type=\"time\"");
        buffer.appendAttribute("name", getName());
        buffer.appendAttribute("id", getId());

        if (getLocalTime() != null) {
            buffer.appendAttribute("value", getLocalTime().format(ISO_FORMATTER));
        } else if (getValue() != null && getValue().length() > 0) {
            buffer.appendAttribute("value", getValue());
        }

        appendAttributes(buffer);
        buffer.append("/>");
    }
}
