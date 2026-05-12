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

import java.lang.reflect.Array;
import java.lang.reflect.Member;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;

import org.apache.commons.lang3.Validate;

import ognl.OgnlOps;
import ognl.OgnlRuntime;
import ognl.TypeConverter;

/**
 * Provides an OGNL TypeConverter class.
 * <p>
 * This class is adapted from the OGNL <code>DefaultTypeConverter</code>, by
 * Luke Blanshard and Drew Davidson, and provides additional Date conversion
 * capabilities.
 */
public class OGNLTypeConverter implements TypeConverter {

    // Thread-safe formatter for Locale-specific date parsing
    private static final DateTimeFormatter DATE_FORMATTER
            = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    // Public Methods ---------------------------------------------------------
    /**
     * Converts the given value to a given type. The OGNL context, target,
     * member and name of property being set are given. This method should be
     * able to handle conversion in general without any context, target, member
     * or property name specified.
     *
     * @param context OGNL context under which the conversion is being done
     * @param target target object in which the property is being set
     * @param member member (Constructor, Method or Field) being set
     * @param propertyName property name being set
     * @param value value to be converted
     * @param toType type to which value is converted
     * @return Converted value of type toType or
     * TypeConverter.NoConversionPossible to indicate that the conversion was
     * not possible.
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Object convertValue(ognl.OgnlContext context, // Changed from Map to OgnlContext
            Object target,
            Member member,
            String propertyName,
            Object value,
            Class toType) {

        return convertValue(value, toType);
    }

    // ------------------------------------------------------ Protected Methods
    /**
     * Return the converted value for the given value object and target type.
     *
     * @param value the value object to convert
     * @param toType the target class type to convert the value to
     * @return a converted value into the specified type
     */
    protected Object convertValue(Object value, Class<?> toType) {
        if (value == null) {
            return toType.isPrimitive() ? OgnlRuntime.getPrimitiveDefaultValue(toType) : null;
        }

        // Handle Array to Array conversion
        if (value.getClass().isArray() && toType.isArray()) {
            Class<?> componentType = toType.getComponentType();
            int length = Array.getLength(value);
            Object result = Array.newInstance(componentType, length);
            for (int i = 0; i < length; i++) {
                Array.set(result, i, convertValue(Array.get(value, i), componentType));
            }
            return result;
        }

        // Numeric Conversions
        if (toType == Integer.class || toType == Integer.TYPE) {
            return (int) OgnlOps.longValue(value);
        }
        if (toType == Double.class || toType == Double.TYPE) {
            return OgnlOps.doubleValue(value);
        }
        if (toType == Boolean.class || toType == Boolean.TYPE) {
            return Boolean.valueOf(value.toString());
        }
        if (toType == Long.class || toType == Long.TYPE) {
            return OgnlOps.longValue(value);
        }
        if (toType == Float.class || toType == Float.TYPE) {
            return (float) OgnlOps.doubleValue(value);
        }
        if (toType == BigInteger.class) {
            return OgnlOps.bigIntValue(value);
        }
        if (toType == BigDecimal.class) {
            return bigDecValue(value);
        }
        if (toType == String.class) {
            return OgnlOps.stringValue(value);
        }

        // Date Conversions using modernized getTimeFromDateString
        if (toType == java.util.Date.class || toType == java.sql.Date.class
                || toType == java.sql.Time.class || toType == java.sql.Timestamp.class) {

            long time = getTimeFromDateString(value.toString());
            if (time == Long.MIN_VALUE) {
                return null;
            }

            if (toType == java.util.Date.class) {
                return new java.util.Date(time);
            }
            if (toType == java.sql.Date.class) {
                return new java.sql.Date(time);
            }
            if (toType == java.sql.Time.class) {
                return new java.sql.Time(time);
            }
            if (toType == java.sql.Timestamp.class) {
                return new java.sql.Timestamp(time);
            }
        }

        return null;
    }

    /**
     * Return the time value in milliseconds of the given date value string, or
     * Long.MIN_VALUE if the date could not be determined.
     *
     * @param value the date value string
     * @return the time value in milliseconds or Long.MIN_VALUE if not
     * determined
     */
    protected long getTimeFromDateString(String value) {
        Validate.notNull(value, "Null value string");
        value = value.trim();
        if (value.isEmpty()) {
            return Long.MIN_VALUE;
        }

        // 1. Raw Epoch Timestamp
        if (isTimeValue(value)) {
            return Long.parseLong(value);
        }

        // 2. ISO SQL Format (yyyy-MM-dd)
        try {
            return java.sql.Date.valueOf(value).getTime();
        } catch (IllegalArgumentException ignored) {
        }

        // 3. Locale-specific Medium Format (Thread-safe)
        try {
            LocalDate date = LocalDate.parse(value, DATE_FORMATTER);
            return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (DateTimeParseException e) {
            return Long.MIN_VALUE;
        }
    }

    /**
     * Return true if the given string value is a long time value.
     *
     * @param value the string value to test
     * @return true if the given string value is a long time value.
     */
    protected boolean isTimeValue(String value) {
        if (value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (i == 0 && c == '-') {
                continue;
            }
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Convert the given value into a BigDecimal.
     *
     * @param value the object to convert into a BigDecimal
     * @return the converted BigDecimal value
     */
    private BigDecimal bigDecValue(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? BigDecimal.ONE : BigDecimal.ZERO;
        }
        return new BigDecimal(value.toString().trim());
    }
}
