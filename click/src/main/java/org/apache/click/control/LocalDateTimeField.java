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
package org.apache.click.control;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.apache.click.util.HtmlStringBuffer;

/**
 * Provides a modern HTML5 LocalDateTime Field control:
 * <input type='datetime-local'>.
 */
public class LocalDateTimeField extends TextField {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    protected LocalDateTime localDateTime;

    public LocalDateTimeField(String name) {
        super(name);
    }

    public LocalDateTimeField(String name, String label) {
        super(name, label);
    }

    public LocalDateTimeField() {
        super();
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
        if (localDateTime != null) {
            super.setValue(localDateTime.format(ISO_FORMATTER));
        } else {
            super.setValue(null);
        }
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        if (value != null && value.trim().length() > 0) {
            try {
                // Safeguard against variations that use space instead of strict 'T'
                String isoValue = value.trim().replace(" ", "T");
                this.localDateTime = LocalDateTime.parse(isoValue, ISO_FORMATTER);
            } catch (DateTimeParseException e) {
                this.localDateTime = null;
            }
        } else {
            this.localDateTime = null;
        }
    }

    @Override
    public Object getValueObject() {
        return getLocalDateTime();
    }

    @Override
    public void setValueObject(Object object) {
        if (object == null) {
            setLocalDateTime(null);
        } else if (object instanceof LocalDateTime) {
            setLocalDateTime((LocalDateTime) object);
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
                String isoValue = getValue().trim().replace(" ", "T");
                LocalDateTime.parse(isoValue, ISO_FORMATTER);
            } catch (DateTimeParseException pe) {
                Object[] args = new Object[]{getErrorLabel(), "yyyy-MM-ddTHH:mm"};
                setError(getMessage("localdatetime-format-error", args));
            }
        }
    }

    @Override
    public void render(HtmlStringBuffer buffer) {
        buffer.append("<input type=\"datetime-local\"");
        buffer.appendAttribute("name", getName());
        buffer.appendAttribute("id", getId());

        if (getLocalDateTime() != null) {
            buffer.appendAttribute("value", getLocalDateTime().format(ISO_FORMATTER));
        } else if (getValue() != null && getValue().length() > 0) {
            buffer.appendAttribute("value", getValue());
        }

        appendAttributes(buffer);
        buffer.append("/>");
    }
}
