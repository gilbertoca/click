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

import java.time.LocalDateTime;
import junit.framework.TestCase;
import org.apache.click.MockContext;

import org.apache.click.servlet.MockRequest;

/**
 * Provides LocalDateTimeField JUnit TestCase.
 */
public class LocalDateTimeFieldTest extends TestCase {

    /**
     * Test that empty, blank, or missing parameters are bound to null.
     */
    public void testNullParameter() {
        MockContext mockContext = MockContext.initContext();
        MockRequest request = mockContext.getMockRequest();

        LocalDateTimeField localDateTimeField = new LocalDateTimeField("dateTimeField");
        assertEquals("dateTimeField", localDateTimeField.getName());

        // Test empty string parameter
        request.getParameterMap().put("dateTimeField", new String[]{""});
        assertTrue(localDateTimeField.onProcess());
        assertNull(localDateTimeField.getLocalDateTime());

        // Test whitespace string parameter
        request.getParameterMap().put("dateTimeField", new String[]{" "});
        assertTrue(localDateTimeField.onProcess());
        assertNull(localDateTimeField.getLocalDateTime());

        // Test null parameter mapping
        request.getParameterMap().put("dateTimeField", null);
        assertTrue(localDateTimeField.onProcess());
        assertNull(localDateTimeField.getLocalDateTime());

        // Test programmatically setting null
        localDateTimeField.setLocalDateTime(null);
        assertNull(localDateTimeField.getLocalDateTime());
    }

    /**
     * Test successful parameter binding, object caching, and space-to-T
     * handling.
     */
    public void testLocalDateTimeCacheValue() {
        MockContext mockContext = MockContext.initContext();
        MockRequest request = mockContext.getMockRequest();

        LocalDateTimeField localDateTimeField = new LocalDateTimeField("dateTimeField");

        // Testing strict ISO layout received from compliant browsers
        String requestParam = "2026-08-26T16:30:00";
        request.getParameterMap().put("dateTimeField", new String[]{requestParam});

        assertTrue(localDateTimeField.onProcess());
        assertEquals(requestParam, localDateTimeField.getValue());

        LocalDateTime expectedDateTime = LocalDateTime.of(2026, 8, 26, 16, 30, 0);
        LocalDateTime date = localDateTimeField.getLocalDateTime();
        assertEquals(expectedDateTime, date);

        // Assert cached reference returns the exact same object block instance
        assertSame(date, localDateTimeField.getLocalDateTime());
        assertSame(date, localDateTimeField.getValueObject());

        // Test alternative string representations (space instead of T delimiter)
        request.getParameterMap().put("dateTimeField", new String[]{"2026-08-26 16:30:00"});
        assertTrue(localDateTimeField.onProcess());
        assertEquals(expectedDateTime, localDateTimeField.getLocalDateTime());
    }

    /**
     * Test that invalid HTML5 datetime string payloads flag validation failures
     * cleanly.
     */
    public void testInvalidDateTimeFormat() {
        MockContext mockContext = MockContext.initContext();
        MockRequest request = mockContext.getMockRequest();

        LocalDateTimeField localDateTimeField = new LocalDateTimeField("dateTimeField");
        request.getParameterMap().put("dateTimeField", new String[]{"26/08/2026 16:30"}); // Invalid syntax block

        assertTrue(localDateTimeField.onProcess());

        // The field should capture parsing exceptions and trigger invalid state flags cleanly
        assertNull(localDateTimeField.getLocalDateTime());
        assertFalse(localDateTimeField.isValid());
    }

    /**
     * Test that the component produces valid HTML5 target string render
     * outputs.
     */
    public void testRender() {
        MockContext.initContext();
        LocalDateTimeField localDateTimeField = new LocalDateTimeField("dateTimeField");
        localDateTimeField.setLocalDateTime(LocalDateTime.of(2026, 8, 26, 16, 30, 0));

        String html = localDateTimeField.toString();

        // Verify native HTML5 specifications inside engine output string
        assertTrue(html.contains("type=\"datetime-local\""));
        assertTrue(html.contains("name=\"dateTimeField\""));
        assertTrue(html.contains("value=\"2026-08-26T16:30:00\""));
    }
}
